(ns saison.core-test
  (:require [saison.core :as sut]
            [clojure.test :as t]
            [clojure.spec.alpha :as s]))

(t/deftest path-specs
  (t/is (s/valid? :saison.core/path
                  #:saison.path {:path "/"
                                 :short-name "root"
                                 :generator 'saison/not-real}))
  (t/is (s/valid? :saison.core/path #:saison.path{:path "/hello"
                                                  :short-name "something"
                                                  :generator 'do/wat
                                                  :data {:woah :buddy}})))
