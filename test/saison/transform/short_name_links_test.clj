(ns saison.transform.short-name-links-test
  (:require [clojure.string :as str]
            [clojure.test :as t :refer [deftest is]]
            [saison.content :as content]
            [saison.path :as path]
            [saison.proto :as proto]
            [saison.source.data :as data]
            [saison.transform.short-name-links :as sut]))

(deftest short-name-links-test
  (let [src (sut/short-name-links
             (data/data-source
              {:path "/index.html"
               :data "<a href=\"abt\">about</a> <a href=\"thr\">there</a>"}
              {:path "/about.html"
               :data ""
               :metadata {:short-name "abt"}}
              {:path "/thereee"
               :data ""
               :metadata {:short-name "thr"}}))
        paths (proto/scan src)
        path (path/find-by-path paths "/index.html")
        content (-> path
                    (path/path->content paths {})
                    content/content->string)]
    (is (str/index-of content "href=\"/about.html\""))
    (is (str/index-of content "href=\"/thereee\""))))
