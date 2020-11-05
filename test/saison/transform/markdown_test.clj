(ns saison.transform.markdown-test
  (:require [clojure.string :as str]
            [clojure.test :as t :refer [deftest is]]
            [saison.transform.markdown :as sut]
            [saison.content :as content]
            [saison.path :as path]
            [saison.source.data :as data]
            [saison.proto :as proto]))

(deftest markdown-test
  (let [src (sut/markdown
             (data/data-source
              {:path "/index.md"
               :data "# Hello"}))
        paths (proto/scan src)
        path (path/find-by-path paths "/index.html")
        content (-> path
                    (path/content paths src)
                    content/string)]
    (is (str/index-of content "<h1>"))
    (is (str/index-of content "Hello"))))

(deftest markdown-metadata-test
  (let [src (sut/markdown (data/data-source
                           {:path "/index.md"
                            :metadata {:extra true}
                            :data "Title: a title
Title: a second title
Date: 2020-09-25

# a blog post

This is some content

- a list
- with stuff

## subheading

bye"}))
        paths (proto/scan src)
        path (path/find-by-path paths "/index.html")
        metadata (path/metadata path paths {})
        content (content/string (path/content path paths {}))]
    ;; metadata comes from the original path
    (is (true?
         (:extra metadata)))
    ;; metadata comes from markdown metadata
    (is (= "a title"
           (:title metadata)))
    ;; the mime type is set to html
    (is (= "text/html"
           (path/mime-type metadata)))))
