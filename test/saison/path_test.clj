(ns saison.path-test
  (:require [saison.path :as sut]
            [saison.source.data :as data]
            [clojure.test :as t :refer [deftest is]]
            [clojure.spec.alpha :as s]
            [saison.proto :as proto]))

(deftest common-metadata
 (is (s/valid? :saison.path/metadata
                {:short-name "fancy"
                 :title "feast"})))

(deftest derive-path-metadata
  (let [paths (data/literal-paths {:path "/hello"
                               :metadata {:a true}
                               :data "hi."})
        hello (first paths)
        derived (sut/derive-path
                 hello {:metadata (fn [o] (assoc (proto/metadata o)
                                                 :b "new"))})]
    (is (= {:a true
            :b "new"}
           (proto/metadata derived)))))
