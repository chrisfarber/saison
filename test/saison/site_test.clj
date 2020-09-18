(ns saison.site-test
  (:require [clojure.test :as t :refer [deftest is]]
            [saison.proto :as proto]
            [saison.site :as sut]
            [saison.source.data :refer [data-source]]
            [saison.util :as util]
            [saison.content :as content]))

(def test-site-1
  {:sources [(data-source
              {:path "/index.html"
               :data "this is index"}
              {:path "/robots.txt"
               :data "hi robots"})
             (data-source
              {:path "/alpha"
               :data "alpha"})]})

(deftest discover-paths-simple
  (let [site test-site-1
        paths (sut/discover-paths site)
        path (first paths)
        path-data (content/content->string (proto/content path paths site))]
    (is (= 3 (count paths)))
    (is (= "/index.html" (proto/path path)))
    (is (= path-data
           "this is index"))))
