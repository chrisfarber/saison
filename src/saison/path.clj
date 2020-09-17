(ns saison.path
  "Functions for manipulating paths and collections of paths."
  (:require [clojure.spec.alpha :as s]
            [saison.proto :as proto]))

(defrecord MappedPath
           [original map-path map-metadata map-content]

  proto/Path
  (path [this]
    (if map-path
      (map-path original)
      (proto/path original)))

  (metadata [this]
    (if map-metadata
      (map-metadata original)
      (proto/metadata original)))

  (content [this paths site]
    (if map-content
      (map-content original paths site)
      (proto/content original paths site))))

(s/def ::full-path string?)
(s/def ::short-name string?)
(s/def ::mime-type string?)

(s/def ::data map?)

(s/def ::title string?)
(s/def ::date-created inst?)
(s/def ::date-updated inst?)

(s/def ::metadata
  (s/keys :opt-un [::short-name
                   ::title
                   ::mime-type
                   ::date-created
                   ::date-updated]))

(defn derive-path
  [path-inst {:keys [path metadata content]}]
  (map->MappedPath
   {:original path-inst
    :map-path path
    :map-metadata metadata
    :map-content content}))

(defn find-by-path
  "Given a list of paths, find the first exact match"
  [paths path-name]
  (first (filter #(= path-name (proto/path %)) paths)))

(defn short-name-expansion-map
  [paths]
  (reduce (fn [short-names path]
            (let [short-name (-> path
                                 proto/metadata
                                 :short-name)]
              (if short-name
                (assoc short-names short-name (proto/path path))
                short-names)))
          {}
          paths))
