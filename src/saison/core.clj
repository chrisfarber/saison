(ns saison.core
  (:require [clojure.spec.alpha :as s]
            [saison.util :as util]))

(s/def ::callable
  (s/or :symbol qualified-symbol?
        :fn var?
        :fn fn?))

;; Paths
;; ==================================================
;; Paths are maps that describe precise, individual pages
;; that can be generated.

(s/def :saison.path/path string?)
(s/def :saison.path/short-name string?)

(s/def :saison.path/generator ::callable)
(s/def :saison.path/data map?)

(s/def :saison.core/path
  (s/keys :req-un [:saison.path/path
                   :saison.path/generator]
          :opt-un [:saison.path/short-name
                   :saison.path/data]))

;; Generator
;; ==================================================
;; generators take a Path map and the site context and write out
;; the content

;; (s/def :saison.core/generator (s/fspec :args (s/cat :path)))

;; Source
;; ==================================================
;; these identify and output paths.

(s/def :saison.source/type ::callable)

(s/def :saison.core/source
  (s/keys :req-un [:saison.source/type]))

;; Site
;; ==================================================

(s/def :saison.site/name string?)
(s/def :saison.site/output string?)
(s/def :saison.site/sources (s/* :saison.core/source))

;; 
;; ==================================================

(defn discover-paths
  "Given the definition of a site, discover all paths."
  [site]
  (mapcat (fn [source]
            (util/invoke (:type source) source))
          (:sources site)))

(s/fdef discover-paths
  :args (s/cat :site :saison.core/site)
  :ret (s/* :saison.core/path))

(defn compile-path
  "Compile the given path using its generator"
  [site paths path]
  (util/invoke (:generator path)
               site paths path))
