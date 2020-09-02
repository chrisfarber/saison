(ns saison.path
  "Functions for manipulating paths and collections of paths."
  (:require [clojure.spec.alpha :as s]
            [saison.proto :as proto]))

(defn- wrap
  [fn-or-nil value]
  ((or fn-or-nil identity) value))

(defrecord MappedPath
           [original map-path map-metadata map-generate]

  proto/Path
  (url-path [this]
    (wrap map-path (proto/url-path original)))

  (metadata [this]
    (wrap map-metadata (proto/metadata original)))

  (generate [this paths site]
    (wrap map-generate (proto/generate original paths site))))

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
