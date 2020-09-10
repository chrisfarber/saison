(ns saison.source.file
  (:require [saison.proto :as types]
            [saison.path :as path]
            [saison.source :as source]
            [saison.util :as util]
            [saison.proto :as types]
            [saison.proto :as proto]))

(defrecord FilePath
           [file base-path path metadata]
  types/Path
  (url-path [this] (util/add-path-component base-path path))
  (metadata [this] metadata)
  (generate [this paths site]
    file))

(defrecord FileSource
           [file-root base-path metadata]

  types/Source
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
    (fn [] "todo ... close...")))

(defn files
  "create a source from files on the filesystem"
  [{:keys [root
           base-path
           metadata]}]
  (map->FileSource
   {:file-root root
    :base-path base-path
    :metadata metadata}))
