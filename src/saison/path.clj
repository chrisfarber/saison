(ns saison.path
  "Paths are maps that describe precise, individual pages
   that can be generated and referred to by other pages.
   
   In addition to capturing the data (and processes) necessary to
   generate the content, the path also captures some optional metadata."
  (:require [clojure.spec.alpha :as s]
            [saison.callable]))

(s/def ::full-path string?)
(s/def ::short-name string?)

(s/def ::generator :saison.callable/ref)
(s/def ::data map?)

(s/def ::title string?)
(s/def ::date-created inst?)
(s/def ::date-updated inst?)

(s/def ::metadata
  (s/keys :opt-un [::title
                   ::date-created
                   ::date-updated]))

(s/def ::path
  (s/keys :req-un [::full-path
                   ::generator]
          :opt-un [::short-name
                   ::data
                   ::metadata]))

(defn short-name
  [path]
  (:short-name path))

(defn path-data
  [path]
  (:data path))
