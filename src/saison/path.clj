(ns saison.path
  "Functions for manipulating paths and collections of paths."
  (:require [saison.proto :as proto]))

(defn pathname
  "returns the url path of the given path"
  [path]
  (proto/pathname path))

(defn metadata
  "metadata for the given path."

  ([path]
   (proto/metadata path)))

(defn content
  "compute content for the given path."

  ([path]
   (proto/content path)))

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
   
   Accepts a map, `modifiers` with the keys:
   :pathname - a function from a path -> string
   :metadata - a function from a path -> new metadata map
   :content - a function from a path -> new content
   
   All of the keys are optional. Unspecified aspects of a path
   will be unmodified.
   
   Optionally, a `predicate` may be supplied. The transform will
   then only be applied if the predicate is true for an input
   path."
  ([modifiers]
   (fn [path]
     (derive-path path modifiers)))
  ([predicate modifiers]
   (fn [path]
     (if (predicate path)
       (derive-path path modifiers)
       path))))

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
