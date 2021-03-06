(ns saison.transform.markdown-test
  (:require [clojure.string :as str]
            [clojure.test :as t :refer [deftest is]]
            [saison.path :as path]
            [saison.proto :as proto]
            [saison.source :as source]
            [saison.source.data :as data]
            [saison.transform.markdown :as sut]
            [saison.transform.yaml-frontmatter :refer [yaml-frontmatter]]
            [saison.content :as content]))

(deftest markdown-test
  (let [src (source/construct
             (data/source
              {:pathname "/index.md"
               :content "# Hello"})
             (sut/markdown))
        paths (proto/scan src)
        path (path/find-by-path paths "/index.html")
        content (content/string path)]
    (is (str/index-of content "<h1>"))
    (is (str/index-of content "Hello"))))

(deftest markdown-metadata-test
  (let [src (source/construct
             (data/source
              {:pathname "/index.md"
               :metadata {:extra true}
               :content "
---
title: a nice title
date: 2020-09-25
---

# a blog post

This is some content

- a list
- with stuff

## subheading

bye"})
             (yaml-frontmatter :where sut/markdown?)
             (sut/markdown))
        paths (proto/scan src)
        path (path/find-by-path paths "/index.html")
        metadata (path/metadata path)]
    ;; metadata comes from the original path
    (is (true?
         (:extra metadata)))
    ;; metadata comes from markdown metadata
    (is (= "a nice title"
           (:title metadata)))
    ;; the mime type is set to html
    (is (= "text/html"
           (path/mime-type metadata)))))

(deftest markdown-with-no-content-outputs-empty-string
  (let [src (source/construct
             (data/source {:pathname "/hi.md"
                           :content ""})
             (sut/markdown))
        path (path/find-by-path (proto/scan src) "/hi.html")
        content (content/string path)]
    (is (= "" content))))
