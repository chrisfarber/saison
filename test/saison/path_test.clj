(ns saison.path-test
  (:require saison.path
            [clojure.test :as t :refer [deftest is]]
            [clojure.spec.alpha :as s]))

(deftest common-metadata
  (let [meta1 {:short-name "fancy"
               :title "feast"}]
    (is (s/valid? :saison.path/metadata
                  meta1))))
