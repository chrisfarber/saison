(ns saison.transform.markdown-test
  (:require [saison.transform.markdown :as sut]
            [clojure.test :as t :refer [deftest is]]
            [saison.site :as sn]
            [clojure.java.io :as io]
            [clojure.string :as str]
            [saison.source.data :as data]
            [saison.proto :as proto]
            [saison.path :as path]
            [saison.util :as util]
            [saison.content :as content]))

(deftest source-test
  (let [src (sut/markdown
             (data/data-source
              {:path "/index.md"
               :data "# Hello"}))
        paths (proto/scan src)
        path (path/find-by-path paths "/index.html")
        content (-> path
                    (proto/content paths src)
                    content/content->string)]
    (is (str/index-of content "<h1>"))
    (is (str/index-of content "Hello"))))


