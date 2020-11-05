(ns saison.site-test
  (:require [clojure.test :as t :refer [deftest is]]
            [saison.content :as content]
            [saison.proto :as proto]
            [saison.site :as sut]
            [saison.source :as source]
            [saison.source.data :refer [data-source]]
            [saison.path :as path]))

(def test-site-1
  {:source (source/combine
            (data-source
             {:path "/index.html"
              :data "this is index"}
             {:path "/robots.txt"
              :data "hi robots"})
            (data-source
             {:path "/alpha"
              :data "alpha"}))})

(deftest discover-paths-simple
  (let [site test-site-1
        paths (sut/discover-paths site)
        path (first paths)
        path-data (content/string (path/content path paths site))]
    (is (= 3 (count paths)))
    (is (= "/index.html" (path/pathname path)))
    (is (= path-data
           "this is index"))))
