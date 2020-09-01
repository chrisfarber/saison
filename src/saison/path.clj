(ns saison.path
  "Functions for manipulating paths and collections of paths."
  (:require [clojure.spec.alpha :as s]
            [saison.proto :as proto]))

(defrecord MappedPath
           [origin map-path map-meta]

  proto/Path
  (url-path [this]
    (proto/url-path origin))

  (metadata [this]
    (proto/metadata origin))

  (generate [this paths site]
    (proto/generate origin paths site)))

(s/def ::full-path string?)
(s/def ::short-name string?)

(s/def ::data map?)

(s/def ::title string?)
(s/def ::date-created inst?)
(s/def ::date-updated inst?)

(s/def ::metadata
  (s/keys :opt-un [::short-name
                   ::title
                   ::date-created
                   ::date-updated]))

(defn find-by-path
  "Given a list of paths, find the first exact match"
  [paths path-name]
  (first (filter #(= path-name (proto/url-path %)) paths)))
