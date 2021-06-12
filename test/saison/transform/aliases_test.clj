(ns saison.transform.aliases-test
  (:require [clojure.string :as str]
            [clojure.test :as t :refer [deftest is]]
            [saison.path :as path]
            [saison.proto :as proto]
            [saison.source :as source]
            [saison.source.data :as data]
            [saison.transform.aliases :as sut]
            [saison.content :as content]))

(deftest aliases-test
  (let [src (source/construct
             (data/source
              {:pathname "/index.html"
               :metadata {:mime-type "text/html"}
               :content "<a href=\"abt\">about</a> <a href=\"thr\">there</a>"}
              {:pathname "/about.html"
               :content ""
               :metadata {:alias "abt"}}
              {:pathname "/thereee"
               :content ""
               :metadata {:alias "thr"}})
             (sut/resolve-path-aliases))
        paths (proto/scan src)
        path (path/find-by-path paths "/index.html")
        content (content/string (path/content path))]
    (is (str/index-of content "href=\"/about.html\""))
    (is (str/index-of content "href=\"/thereee\""))))
