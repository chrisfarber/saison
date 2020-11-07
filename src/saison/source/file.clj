(ns saison.source.file
  (:require [hawk.core :as hawk]
            [saison.proto :as proto]
            [saison.util :as util]))

(def mime-types
  {"htm" "text/html"
   "html" "text/html"
   "css" "text/css"
   "edn" "application/edn"
   "ico" "image/x-icon"
   "js" "text/javascript"
   "json" "application/json"
   "gz" "application/gzip"
   "png" "image/png"
   "jpg" "image/jpeg"
   "jpeg" "image/jpeg"})

(defrecord FilePath
    [file base-path path metadata]
  proto/Path
  (pathname [this] (util/add-path-component base-path path))
  (metadata [this]
    (let [path (proto/pathname this)
          extension (util/path-extension path)
          known-mime (get mime-types extension)]
      (merge (when known-mime
               {:mime-type known-mime})
             metadata)))
  (content [this]
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
    (let [notifier (fn [_ _]
                     (changed))
          watcher (hawk/watch! [{:paths [file-root]
                                 :handler notifier}])]
      (fn []
        (hawk/stop! watcher)))))

(defn files
  "create a source from files on the filesystem"
  [{:keys [root
           base-path
           metadata]}]
  (map->FileSource
   {:file-root root
    :base-path base-path
    :metadata metadata}))
