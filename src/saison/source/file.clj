(ns saison.source.file
  (:require [saison.path :as path :refer [Path]]
            [saison.source :as source :refer [Source]]
            [saison.util :as util]))

(defrecord FilePath
           [file url-path metadata]
  Path
  (url-path [this] url-path)
  (metadata [this] metadata)
  (generate [this paths site]
    file))

(defrecord FileSource
           [file-root base-path metadata]
  Source
  (scan [this]
    (let [files (util/list-files file-root)]
      (map (fn [[name f]]
             (map->FilePath {:file f
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
           meta]}]
  (map->FileSource
   {:file-root root
    :base-path base-path
    :metadata meta}))

(comment
  (satisfies? Path
              (first (source/scan (map->FileSource {:file-root "./"})))))
