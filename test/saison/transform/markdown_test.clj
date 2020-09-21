(ns saison.transform.markdown-test
  (:require [clojure.string :as str]
            [clojure.test :as t :refer [deftest is]]
            [saison.transform.markdown :as sut]
            [saison.content :as content]
            [saison.path :as path]
            [saison.source.data :as data]
            [saison.proto :as proto]))

(deftest source-test
  (let [src (sut/markdown
             (data/data-source
              {:path "/index.md"
               :data "# Hello"}))
        paths (proto/scan src)
        path (path/find-by-path paths "/index.html")
        content (-> path
                    (path/path->content paths src)
                    content/content->string)]
    (is (str/index-of content "<h1>"))
    (is (str/index-of content "Hello"))))


