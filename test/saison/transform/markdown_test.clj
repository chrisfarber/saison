(ns saison.transform.markdown-test
  (:require [clojure.string :as str]
            [clojure.test :as t :refer [deftest is]]
            [saison.path :as path]
            [saison.proto :as proto]
            [saison.source :as source]
            [saison.source.data :as data]
            [saison.transform.markdown :as sut]))

(deftest markdown-test
  (let [src (source/construct
             (data/source
              {:pathname "/index.md"
               :content "# Hello"})
             (sut/markdown))
        paths (proto/scan src)
        path (path/find-by-path paths "/index.html")
        content (path/content path)]
    (is (str/index-of content "<h1>"))
    (is (str/index-of content "Hello"))))

(deftest markdown-metadata-test
  (let [src (source/construct
             (data/source
              {:pathname "/index.md"
               :metadata {:extra true}
               :content "Title: a title
Title: a second title
Date: 2020-09-25

# a blog post

This is some content

- a list
- with stuff

## subheading

bye"})
             (sut/markdown))
        paths (proto/scan src)
        path (path/find-by-path paths "/index.html")
        metadata (path/metadata path)]
    ;; metadata comes from the original path
    (is (true?
         (:extra metadata)))
    ;; metadata comes from markdown metadata
    (is (= "a title"
           (:title metadata)))
    ;; the mime type is set to html
    (is (= "text/html"
           (path/mime-type metadata)))))
