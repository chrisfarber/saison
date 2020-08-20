(ns saison.core
  (:require [clojure.spec.alpha :as s]
            [saison.path]
            [saison.callable :refer [invoke]]))


;; Generator
;; ==================================================
;; generators take a Path map and the site context and write out
;; the content

;; (s/def :saison.core/generator (s/fspec :args (s/cat :path)))

;; Source
;; ==================================================
;; these identify and output paths.

(s/def :saison.source/type :saison.callable/ref)

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
            (invoke (:type source) source))
          (:sources site)))

(s/fdef discover-paths
  :args (s/cat :site :saison.core/site)
  :ret (s/* :saison.path/path))

(defn compile-path
  "Compile the given path using its generator"
  [site paths path]
  (invoke (:generator path)
          site paths path))
