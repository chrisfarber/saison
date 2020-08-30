(ns saison.path
  "Functions for manipulating paths and collections of paths."
  (:require [clojure.spec.alpha :as s]))

(defprotocol Path
  (url-path [this]
    "retrieve the path component of the URL")
  (metadata [this]
    "retrieve a metadata map for this path")
  (generate [this paths site]
    "compiles the path
    a string, or an input stream, is returned.

    the `paths` argument should be a list of all other paths identified
    for the site. this enables the path to dynamically compute content or
    references to other paths. it is probably not good practice for a path
    to generate another path, at the risk of causing an infinite loop."))

(defrecord MappedPath
           [origin map-path map-meta]

  Path
  (url-path [this]
    (url-path origin))

  (metadata [this]
    (metadata origin))

  (generate [this paths site]
    (generate origin paths site)))

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
  (first (filter #(= path-name (url-path %)) paths)))
