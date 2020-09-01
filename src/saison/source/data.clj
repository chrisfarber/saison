(ns saison.source.data
  (:require [saison.proto :refer [Path Source]]
            [saison.path :as path]
            [saison.source :as source]
            [saison.util :as util]
            [saison.proto :as types]))

(defrecord DataPath
           [data path metadata]
  types/Path
  (url-path [this] path)
  (metadata [this] metadata)
  (generate [this paths site]
    data))

(defrecord DataSource
           [items]

  types/Source
  (scan [this]
    (map map->DataPath items))
  (watch [this cb]
    (fn [])))

(defn data-paths
  "A source of paths from literal data.

  Accepts any number of maps. Each map is expected to have the keys:
  :data
  :path
  :metadata (optional)"
  [& paths]
  (map->DataSource {:items paths}))
