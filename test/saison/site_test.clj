(ns saison.site-test
  (:require [saison.site :as sut]
            [clojure.test :as t :refer [deftest is]]
            [saison.path :as path]
            [saison.source.data :refer [data-paths]]
            [saison.proto :as proto]
            [saison.util :as util]))

(def test-site-1
  {:sources [(data-paths
              {:path "/index.html"
               :data "this is index"}
              {:path "/robots.txt"
               :data "hi robots"})
             (data-paths
              {:path "/alpha"
               :data "alpha"})]})

(deftest discover-paths-simple
  (let [site test-site-1
        paths (sut/discover-paths site)
        path (first paths)
        path-data (util/input-stream->string (proto/generate path paths site))]
    (is (= 3 (count paths)))
    (is (= "/index.html" (proto/url-path path)))
    (is (= path-data
           "this is index"))))

