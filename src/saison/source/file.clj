(ns saison.source.file
  (:require [hawk.core :as hawk]
            [saison.proto :as proto]
            [saison.util :as util]
            [pantomime.mime :refer [mime-type-of]]))

(defrecord FilePath
    [file base-path path metadata]
  proto/Path
  (pathname [this] (util/add-path-component base-path path))
  (metadata [this]
    (let [path (proto/pathname this)
          extension (util/path-extension path)
          known-mime (mime-type-of file)]
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
        (hawk/stop! watcher))))

  (before-build-hook [_ _])

  (before-publish-hook [_ _]))

(defn files
  "create a source from files on the filesystem"
  [{:keys [root
           base-path
           metadata]}]
  (map->FileSource
   {:file-root root
    :base-path base-path
    :metadata metadata}))
