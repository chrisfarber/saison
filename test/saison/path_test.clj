(ns saison.path-test
  (:require [clojure.test :as t :refer [deftest is]]
            [saison.path :as sut]
            [saison.source.data :as data]))

(deftest derive-path-metadata
  (let [paths (data/paths {:pathname "/hello"
                           :metadata {:a true}
                           :content "hi."})
        hello (first paths)
        derived (sut/derive-path
                 hello {:metadata (fn [o] (assoc (sut/metadata o)
                                                 :b "new"))})]
    (is (= {:a true
            :b "new"}
           (sut/metadata derived)))))
