(ns saison.site
  (:require [clojure.spec.alpha :as s]
            [saison.path]
            [saison.callable :refer [invoke]]))

(s/def ::name string?)
(s/def ::output string?)
(s/def ::sources (s/* :saison.core/source))

(s/def ::site (s/keys :req-un [::sources
                               ::name
                               ::output]))

;; 
;; ==================================================

(defn discover-paths
  "Given the definition of a site, discover all paths."
  [site]
  (mapcat (fn [source]
            (invoke (:type source) source))
          (:sources site)))

(s/fdef discover-paths
  :args (s/cat :site ::site)
  :ret (s/* :saison.path/path))

(defn compile-path
  "Compile the given path using its generator"
  [site paths path]
  (invoke (:generator path)
          site paths path))
