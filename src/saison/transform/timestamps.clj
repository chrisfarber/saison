(ns saison.transform.timestamps
  (:require
   [clojure.edn :as edn]
   [clojure.java.io :as io]
   [clojure.pprint :refer [pprint]]
   [saison.path :as path]
   [saison.proto :as proto]
   [saison.source :as source]
   [tick.core :as t]
   [time-literals.read-write]))

(defn read-edn [f not-found]
  (try
    (with-open [rdr (io/reader f)
                pb (java.io.PushbackReader. rdr)]
      (edn/read {:readers time-literals.read-write/tags} pb))
    (catch Exception _
      not-found)))

(defn write-edn [f data]
  (with-open [wtr (io/writer f)]
    (binding [*out* wtr]
      (pprint data))))

(defn read-db
  [db-path]
  (read-edn (io/file db-path)
            {}))

(defn write-db
  [db-path data]
  (write-edn (io/file db-path) data))

(defn save-missing-timestamp [db timestamp pathname key]
  (let [existing-ts (get-in @db [pathname key])]
    (if (some? existing-ts)
      existing-ts
      (do
        (swap! db update-in [pathname] assoc key timestamp)
        timestamp))))

(defn add-timestamps-to-metadata
  [ts-db]
  (path/transformer
   :name "timestamps"
   :cache false
   :metadata (fn [path]
               (let [pathname (path/pathname path)
                     metadata (path/metadata path)]
                 (assoc metadata
                        :created-at (get-in @ts-db [pathname :created-at])
                        :published-at (get-in @ts-db [pathname :published-at]))))))

(defn save-missing-timestamps
  [ts-db paths key]
  (let [ts (t/zoned-date-time)]
    (doseq [path paths]
      (save-missing-timestamp ts-db ts (path/pathname path) key))))

(defn timestamp-database
  [& {:keys [path]}]
  (let [db (atom (read-db path))]
    (source/steps
     (source/transform-paths (add-timestamps-to-metadata db) :cache false)
     (source/before-build (fn [{:keys [source]}]
                            (save-missing-timestamps db (proto/scan source) :created-at)
                            (write-db path @db)))
     (source/before-publish (fn [{:keys [source]}]
                              (save-missing-timestamps db (proto/scan source) :published-at)
                              (write-db path @db))))))
