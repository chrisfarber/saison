(ns saison.source.data
  (:require [saison.proto :as proto :refer [scan Path Source]]
            [saison.source :as source]))

(defn- resolve [thing]
  (if (fn? thing)
    (thing)
    thing))

(defrecord DataPath
    [pathname metadata content]
  Path
  (pathname [this]
    (resolve pathname))
  (metadata [this]
    (resolve metadata))
  (content [this]
    (resolve content)))

(defn path
  "create a literal path. should receive a map containing the keys
  :pathname
  :metadata (optional)
  :content"
  [path-def]
  (map->DataPath path-def))

(defn paths
  "create a list of paths by applying `path` it its inputs.
  the provided path-defs are flattened."
  [& path-defs]
  (map path (flatten path-defs)))

(defn source
  "A source of paths from literal data.

  Accepts any number of maps. Each map is expected to have the keys:
  :pathname
  :metadata (optional)
  :content"
  [& path-defs]
  (source/construct
    (emit (paths path-defs))))
