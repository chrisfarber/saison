(ns saison.site
  (:require [clojure.spec.alpha :as s]
            [saison.path :as path]
            [saison.proto :as proto]))

(s/def ::name string?)
(s/def ::output string?)
(s/def ::source fn?)

(s/def ::site (s/keys :req-un [::source
                               ::name
                               ::output]))

;; 
;; ==================================================

(defn discover-paths
  "Given the definition of a site, discover all paths."
  [site]
  (proto/scan (:source site)))

(s/fdef discover-paths
  :args (s/cat :site ::site)
  :ret (s/* :saison.path/path))

