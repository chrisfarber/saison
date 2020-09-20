(ns saison.source.file
  (:require [saison.proto :as proto]
            [saison.util :as util]
            [juxt.dirwatch :refer [watch-dir close-watcher]]
            [clojure.java.io :as io]))

(def mime-types
  {"htm" "text/html"
   "html" "text/html"
   "css" "text/css"
   "edn" "application/edn"
   "ico" "image/x-icon"
   "js" "text/javascript"
   "json" "application/json"
   "gz" "application/gzip"})

(defrecord FilePath
           [file base-path path metadata]
  proto/Path
  (path [this] (util/add-path-component base-path path))
  (metadata [this]
    (let [path (proto/path this)
          extension (util/path-extension path)
          known-mime (get mime-types extension)]
      (merge (when known-mime
               {:mime-type known-mime})
             metadata)))
  (content [this paths site]
    file))

(defrecord FileSource
           [file-root base-path metadata]

  proto/Source
  (scan [this]
    (let [files (util/list-files file-root)]
      (map (fn [[name f]]
             (map->FilePath {:file f
                             :base-path base-path
                             :path name
                             :metadata metadata}))
           files)))

  (watch
    [this changed]
    (println "watching.")
    (let [watcher (watch-dir (fn [& args]
                               (println "received update" args)
                               (changed))
                             (io/as-file file-root))]
      (fn []
        (println "closing.")
        (close-watcher watcher)))))

(defn files
  "create a source from files on the filesystem"
  [{:keys [root
           base-path
           metadata]}]
  (map->FileSource
   {:file-root root
    :base-path base-path
    :metadata metadata}))
