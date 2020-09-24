(ns saison.path
  "Functions for manipulating paths and collections of paths."
  (:require [clojure.spec.alpha :as s]
            [saison.proto :as proto]))

(def ^:dynamic *paths*
  "bound to a seq of other paths that have been discovered within the site"
  nil)

(def ^:dynamic *env*
  "bound to a map of environment info about the site"
  nil)

(defn path->name
  "returns the url path of the given path"
  [path]
  (proto/path path))

(defn path->metadata
  "metadata for the given path.

  if `paths` and `env` are suppplied, they will automatically be bound
  to `*paths*` and `*env*`, respectively."

  ([path]
   (proto/metadata path))
  ([path paths env]
   (binding [*paths* path
             *env* env]
     (proto/metadata path))))

(defn path->content
  "compute content for the given path.

  if `paths` and `env` are suppplied, they will automatically be bound
  to `*paths*` and `*env*`, respectively."

  ([path]
   (proto/content path))
  ([path paths env]
   (binding [*paths* paths
             *env* env]
     (proto/content path))))

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

  (content [this]
    (if map-content
      (map-content original)
      (proto/content original))))

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

(defn- value-for-binding
  [method bind-sym path-sym]
  (let [lookup-path `(path->name ~path-sym)
        lookup-metadata `(path->metadata ~path-sym)
        lookup-content `(path->content ~path-sym)
        lookup {'path {'path lookup-path}
                'metadata {'path lookup-path
                           'metadata lookup-metadata}
                'content {'path lookup-path
                          'metadata lookup-metadata
                          'content lookup-content}}
        found (get-in lookup [method bind-sym])]
    (or
      found
      (throw (ex-info "Unknown binding" {:method method
                                         :binding bind-sym
                                         :allowed-bindings (keys (get lookup method))})))))

(defn- transform-method-form
  "convert a transformation form to a key/fn pair suitable for passing `derive-path`"
  [form]
  (let [path-sym (gensym "path-")
        [method bindings & method-body] form
        bindings (mapcat (fn [dep]
                           [dep (value-for-binding method dep path-sym)])
                         bindings)]
    (when-not (#{'path 'metadata 'content} method)
      (throw (ex-info (str "Unknown method") {:method method
                                              :form form})))
    [(keyword method) `(fn [~path-sym]
                         (let [~@bindings]
                           ~@method-body))]))

(defmacro deftransform
  "create a path transformer. defines a function of either:
    [path, args..] -> path
  or
    path -> [args...] -> path

  The first form is a binding vector of arguments.

  Subsequent forms resemble record method definitions, EXCEPT:
  - the function name must be one of `path`, `metadata`, or `content`.
  - the bindings vector can only contain the same set of symbols
  - the values of those symbols will be bound to the corresponding computed part of the path"

  [transform-name-sym transform-arg-bindings & mapper-definitions]
  (let [path 'path
        transform-seq (map transform-method-form mapper-definitions)
        transforms (into {} transform-seq)]
    `(defn ~transform-name-sym
       ([~@transform-arg-bindings]
        (fn [~path]
          (~transform-name-sym ~path ~@transform-arg-bindings)))
       ([~path ~@transform-arg-bindings]
        (derive-path ~path ~transforms)))))

(defn find-by-path
  "Given a list of paths, find the first exact match"
  [paths path-name]
  (first (filter #(= path-name (path->name %)) paths)))

(defn short-name-expansion-map
  [paths]
  (reduce (fn [short-names path]
            (let [short-name (-> path
                                 path->metadata
                                 :short-name)]
              (if short-name
                (assoc short-names short-name (path->name path))
                short-names)))
          {}
          paths))

(defn mime-type
  "retrieve the mime-type from the path's metadata"
  [path-or-meta]
  (:mime-type (if (satisfies? proto/Path path-or-meta)
                (path->metadata path-or-meta)
                path-or-meta)))

(defn is-html?
  "Return true if the path's metadata indicates it's HTML"
  [path-or-meta]
  (= "text/html"
     (mime-type path-or-meta)))
