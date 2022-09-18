(ns saison.path
  "Functions for manipulating paths and collections of paths."
  (:require [saison.path.caching :refer [cached]]
            [saison.proto :as proto])
  (:refer-clojure :exclude [resolve])
  (:import [java.net URI]))

(defn pathname
  "returns the url path of the given path"
  [path]
  (cond
    (satisfies? proto/Path path)
    (proto/pathname path)

    (string? path) path

    :else (throw (ex-info "Can't make pathname" {:from path}))))

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

(defn handle-meta-and-content
  [original-path derivation]
  (if-let [f (:metadata-and-content derivation)]
    (let [out (delay (f original-path))]
      (assoc derivation
             :metadata (fn [_] (:metadata @out))
             :content (fn [_] (:content @out))))
    derivation))

(defn enhance-derivation [original-path derivation]
  (reduce (fn [derivation enhancer]
            (enhancer original-path derivation))
          derivation
          [handle-meta-and-content]))

(defn transformer
  "Build a path transformer, which is a function that accepts
   a path and returns a new, modified path.

   The following options are accepted:
   :where - a predicate from path -> boolean
   :pathname - a function from a path -> string
   :metadata - a function from a path -> new metadata map
   :content - a function from a path -> new content
   :metadata-and-content - a function from a path -> [meta, content]
   :cache - a boolean indicating whether the path should cache itself
            default true.

   All of the keys are optional. Unspecified aspects of a path
   will be unmodified."
  [& {:keys [where cache name]
      :as opts
      :or {cache true
           where (constantly true)}}]
  (let [derive (if cache
                 (comp cached derive-path)
                 derive-path)]
    (fn [path]
      (if (where path)
        (derive path (enhance-derivation path opts))
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

(defn resolve
  "Given a Path and a relative url path, return a full pathname."
  [path rel-url]
  (let [pathname (pathname path)
        uri (URI. pathname)]
    (str (.resolve uri rel-url))))

(defn canonicalize
  "Given a root URL and a Path or pathname, build a full URL."
  [root-url path-or-pathname]
  (str (.resolve (URI. (str root-url "/"))
                 (str "./" (pathname path-or-pathname)))))

(defn path-info
  "Process a seq of paths into a map structure containing the paths
   name and metadata as regular data. Path content is elided."
  [path-seq]
  (reduce (fn [m path]
            (let [pname (pathname path)]
              (if (contains? m pname)
                m
                (assoc m pname (metadata path)))))
          {}
          path-seq))
