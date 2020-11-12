(ns saison.transform.timestamps
  (:require [saison.source :as source]
            [clojure.edn :as edn]
            [clojure.java.io :as io]
            [time-literals.read-write]
            [saison.proto :as proto]
            [saison.path :as path :refer [deftransform]]
            [tick.alpha.api :as t]))

(defn read-edn [f not-found]
  (try
    (with-open [rdr (io/reader f)
                pb (java.io.PushbackReader. rdr)]
      (edn/read {:readers time-literals.read-write/tags} pb))
    (catch Exception e
      not-found)))

(defn write-edn [f data]
  (with-open [wtr (io/writer f)]
    (binding [*out* wtr]
      (pr data))))

(defn read-db
  [db-path]
  (read-edn (io/file db-path)
            {}))

(defn write-db
  [db-path data]
  (write-edn (io/file db-path) data))

(defn add-missing-timestamp [db timestamp pathname key]
  (let [existing-ts (get-in @db [pathname key])]
    (if (some? existing-ts)
      existing-ts
      (do
        (swap! db update-in [pathname] assoc key timestamp)
        timestamp))))

(deftransform add-timestamps
  [ts-db]
  (metadata [pathname metadata]
    (assoc metadata
           :created-at (get-in @ts-db [pathname :created-at]))))

(defn add-missing-timestamps
  [ts-db paths key]
  (let [ts (t/zoned-date-time)]
    (doseq [path paths]
      (add-missing-timestamp ts-db ts (path/pathname path) :created-at))))

(defn timestamp-database
  [source timestamp-db-path]

  (let [db (atom (read-db timestamp-db-path))]
    (source/construct
      (input source)
      (map (add-timestamps db))
      (before-build [env]
        (add-missing-timestamps db (proto/scan source) :created-at)
        (write-db timestamp-db-path @db))
      (before-publish [env]
        (add-missing-timestamps db (proto/scan source) :published-at)
        (write-db timestamp-db-path @db)))))
