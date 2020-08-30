(ns saison.source
  (:require [clojure.spec.alpha :as s]
            [saison.util :as util]))

(defprotocol Source
  (scan [this])
  (watch [this changed]))

(defrecord MappedSource
    [origin path-mapper]

  Source
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
