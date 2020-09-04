(ns saison.source-test
  (:require [saison.source :as sut]
            [clojure.test :as t :refer [deftest is]]
            [saison.source.data :as data]
            [saison.proto :as proto]))

(deftest concat-sources
  (let [s1 (data/data-source
            {:path "/index.html"
             :data "index"}
            {:path "/robots.txt"
             :data "hi robots"})
        s2 (data/data-source
            {:path "/stuff.md"
             :data "stuff"})
        combined (sut/concat-sources s1 s2)
        outputs (proto/scan combined)]
    (is (= 3 (count outputs)))))

(deftest map-by-file-extension
  (let [data (data/data-source
              {:path "/hello.html"
               :data "stuff"}
              {:path "/bye.md"
               :data "bye"}
              {:path "/other.css"
               :data "eh"}
              {:path "/stuff.md"
               :data "..."})
        mapped-source (sut/map-source-by-file-ext
                       data
                       {".md" identity})
        outputs (proto/scan mapped-source)]
    ;; todo, actually transform the source
    (is (= 4 outputs))))
