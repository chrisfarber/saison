(ns saison.source
  (:require [saison.proto :as types]
            [saison.util :as util]))

(def scan types/scan)
(def watch types/watch)

(defrecord MappedSource
    [origin path-mapper]

  types/Source
  (scan [this]
    (map path-mapper (scan origin)))

  (watch [this changed]
    (watch origin changed)))

(defn map-source
  "derive a new source that applies a mapping fn to all of its paths"
  [source map-fn]
  (map->MappedSource
   {:origin source
    :path-mapper map-fn}))
