(ns saison.source
  (:require [saison.proto :as proto]
            [saison.util :as util]))

(defrecord MappedSource
    [origin path-mapper]

  proto/Source
  (scan [this]
    (map path-mapper (proto/scan origin)))

  (watch [this changed]
    (proto/watch origin changed)))

(defn map-source
  "derive a new source that applies a mapping fn to all of its paths"
  [source map-fn]
  (map->MappedSource
   {:origin source
    :path-mapper map-fn}))
