(ns saison.source-test
  (:require [clojure.test :as t :refer [deftest is]]
            [saison.proto :as proto]
            [saison.source :as sut]
            [saison.source.data :as data]))

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
        xform #(sut/filter-source % (fn [path]
                                      (not= "/bye.md"
                                            (proto/path path))))
        mapped-source (sut/map-source-by-file-ext
                       data
                       {"md" xform})
        outputs (proto/scan mapped-source)]
    (is (= 1 (count outputs)))))
