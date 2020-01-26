(ns saison.core
  (:require [clojure.spec.alpha :as s]))

;; Paths
;; ==================================================
;; Paths are maps that describe precise, individual pages
;; that can be generated.

(s/def :saison.path/path string?)
(s/def :saison.path/short-name string?)
(s/def :saison.path/compile
  fn?
  #_(s/fspec :args (s/cat :path :saison.core/path)))
;; i think that what i'm grappling with here is that there are no good
;; ways to dynamically identify what computation to perform.

(s/def :saison.core/path
  (s/keys :req [:saison.path/path
                :saison.path/compile]
          :opt [:saison.path/short-name]))

;; Generator
;; ==================================================
;; generators take a Path map and some options and write out
;; the content



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
  (s/valid? :saison.core/resolver {:type 'hello})
  (s/valid? :saison.core/path #:saison.path{:path "/hello"
                                            :short-name "something"
                                            :compile (fn [x] 4)})

  (s/valid? :saison.core/path
            #:saison.path
            {:path "/"
             :short-name "root"
             :compile (fn [p] "Hello")})
  )
