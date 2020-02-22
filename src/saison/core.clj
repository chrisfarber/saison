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
  (s/keys :req [:saison.path/path
                :saison.path/generator]
          :opt [:saison.path/short-name
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

(defmulti source :saison.source/type)

(s/def :saison.core/source
  (s/multi-spec source :saison.source/type))

;; Site
;; ==================================================

(s/def :saison.site/name string?)
(s/def :saison.site/output string?)
(s/def :saison.site/sources :saison.core/source)

;; 
;; ==================================================

(defn eh []
  (inc (dec 42)))


(comment
  (s/valid? :saison.core/path #:saison.path{:path "/hello"
                                            :short-name "something"
                                            :generator 'do/wat
                                            :data {:woah :buddy}})

  (s/valid? :saison.core/path
            #:saison.path
            {:path "/"
             :short-name "root"
             :compile (fn [p] "Hello")})
  )
