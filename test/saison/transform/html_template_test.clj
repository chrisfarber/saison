(ns saison.transform.html-template-test
  (:require [clojure.string :as str]
            [clojure.test :as t :refer [deftest is]]
            [saison.content :as content]
            [saison.path :as path]
            [saison.proto :as proto]
            [saison.source.data :as data]
            [saison.transform.html-template :as sut]))

(deftest new-templating-test
  (let [source (sut/templates
                (data/data-source
                 {:path "/hello.html"
                  :metadata {:mime-type "text/html"
                             :title "fancy title"
                             :template "test1"
                             :html-meta-tags {"abcd" "efg"}}
                  :data "<h1>heading</h1><p>content</p>"})
                {:file "fixtures/template/template.html"
                 :name "test1"
                 :content-selector :div#content
                 :edits [sut/apply-html-metadata
                         sut/set-title]})
        paths (proto/scan source)
        path (first paths)
        raw-content (path/content path paths {})
        content (content/content->string raw-content)]
    (is (str/index-of content "<meta name=\"abcd\" content=\"efg\""))
    (is (str/index-of content "<title>fancy title"))
    (is (str/index-of content "<p>content</p"))
    (is (nil? (str/index-of content "id=\"content\"")))))
