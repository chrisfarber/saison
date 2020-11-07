(ns saison.source.data
  (:require [saison.proto :as proto :refer [scan Path Source]]))

(defrecord DataPath
    [pathname metadata content]
  Path
  (pathname [this]
    pathname)
  (metadata [this]
    metadata)
  (content [this]
    content))

(defrecord DataSource
    [items]

  Source
  (scan [this]
    (map map->DataPath items))
  (watch [this cb]
    (fn [])))

(defn source
  "A source of paths from literal data.

  Accepts any number of maps. Each map is expected to have the keys:
  :pathname
  :metadata (optional)
  :content"
  [& paths]
  (map->DataSource {:items paths}))

(defn paths
  "create a list of paths.

  generates a data source from the supplied data using `data-source`, and
  then invokes `scan` on it."
  [& path-defs]
  (-> path-defs
      source
      scan))
