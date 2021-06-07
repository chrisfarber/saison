(ns saison.path
  "Functions for manipulating paths and collections of paths."
  (:require [saison.proto :as proto]))

(def ^:dynamic *paths*
  "bound to a seq of other paths that have been discovered within the site"
  nil)

(def ^:dynamic *env*
  "bound to a map of environment info about the site"
  nil)

(defn pathname
  "returns the url path of the given path"
  [path]
  (proto/pathname path))

(defn metadata
  "metadata for the given path.

  if `paths` and `env` are suppplied, they will automatically be bound
  to `*paths*` and `*env*`, respectively."

  ([path]
   (proto/metadata path))
  ([path paths env]
   (binding [*paths* paths
             *env* env]
     (proto/metadata path))))

(defn content
  "compute content for the given path.

  if `paths` and `env` are suppplied, they will automatically be bound
  to `*paths*` and `*env*`, respectively."

  ([path]
   (proto/content path))
  ([path paths env]
   (binding [*paths* paths
             *env* env]
     (proto/content path))))

(defrecord DerivedPath
           [original map-path map-metadata map-content]

  proto/Path
  (pathname [_]
    (if map-path
      (map-path original)
      (proto/pathname original)))

  (metadata [_]
    (if map-metadata
      (map-metadata original)
      (proto/metadata original)))

  (content [_]
    (if map-content
      (map-content original)
      (proto/content original))))

(defn derive-path
  [path-inst {:keys [pathname metadata content]}]
  (map->DerivedPath
   {:original path-inst
    :map-path pathname
    :map-metadata metadata
    :map-content content}))

(defn transformer
  "Build a path transformer, which is a function that accepts
   a path and returns a new, modified path.
   
   Accepts a map with the keys:
   :pathname - a function from a path -> string
   :metadata - a function from a path -> new metadata map
   :content - a function from a path -> new content
   
   All of the keys are optional. Unspecified aspects of a path
   will be unmodified."
  [modifiers]
  (fn [path]
    (derive-path path modifiers)))


(defn find-by-path
  "Given a list of paths, find the first exact match"
  [paths path-name]
  (first (filter #(= path-name (pathname %)) paths)))

(defn mime-type
  "retrieve the mime-type from the path's metadata"
  [path-or-meta]
  (:mime-type (if (satisfies? proto/Path path-or-meta)
                (metadata path-or-meta)
                path-or-meta)))

(defn html?
  "Return true if the path's metadata indicates it's HTML"
  [path-or-meta]
  (= "text/html"
     (mime-type path-or-meta)))
