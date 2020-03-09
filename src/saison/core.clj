(ns saison.core
  (:require [clojure.spec.alpha :as s]))

;; Paths
;; ==================================================
;; Paths are maps that describe precise, individual pages
;; that can be generated.

(s/def :saison.path/path string?)
(s/def :saison.path/short-name string?)

(s/def :saison.path/generator qualified-symbol?)
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

(s/def :saison.source/type qualified-symbol?)

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
            (let [source-fn (requiring-resolve (:type source))]
              (source-fn source)))
          (:sources site)))

(defn compile-path
  "Compile the given path. TBD."
  [site paths path]
  (let [generator (requiring-resolve (:generator path))]
    (generator site paths path)))
