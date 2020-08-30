(ns saison.source.file
  (:require [saison.path :as path :refer [Path]]
            [saison.source :as source :refer [Source]]
            [saison.util :as util]))

(defrecord FilePath
    [file base-path path metadata]
  Path
  (url-path [this] (util/add-path-component base-path path))
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

(comment
  (satisfies? Path
              (first (source/scan (map->FileSource {:file-root "./"})))))
