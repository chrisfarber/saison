(ns saison.source.data
  (:require [saison.proto :as proto :refer [scan Path Source]]))

(defrecord DataPath
    [data path metadata]
  Path
  (path [this] path)
  (metadata [this] metadata)
  (content [this]
    data))

(defrecord DataSource
    [items]

  Source
  (scan [this]
    (map map->DataPath items))
  (watch [this cb]
    (fn [])))

(defn data-source
  "A source of paths from literal data.

  Accepts any number of maps. Each map is expected to have the keys:
  :data
  :path
  :metadata (optional)"
  [& paths]
  (map->DataSource {:items paths}))

(defn literal-paths
  "create a list of paths.

  generates a data source from the supplied data using `data-source`, and
  then invokes `scan` on it."
  [& path-defs]
  (-> path-defs
      data-source
      scan))
