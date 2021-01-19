(ns saison.path-test
  (:require [clojure.spec.alpha :as s]
            [clojure.test :as t :refer [deftest is]]
            [saison.path :as sut]
            [saison.proto :as proto]
            [saison.source.data :as data]
            [saison.path :as path]))

(deftest common-metadata
  (is (s/valid? :saison.path/metadata
                {:alias "fancy"
                 :title "feast"})))

(deftest derive-path-metadata
  (let [paths (data/paths {:pathname "/hello"
                           :metadata {:a true}
                           :content "hi."})
        hello (first paths)
        derived (sut/derive-path
                 hello {:metadata (fn [o] (assoc (path/metadata o)
                                                 :b "new"))})]
    (is (= {:a true
            :b "new"}
           (path/metadata derived)))))
