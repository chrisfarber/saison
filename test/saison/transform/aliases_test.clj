(ns saison.transform.aliases-test
  (:require [clojure.string :as str]
            [clojure.test :as t :refer [deftest is]]
            [saison.content :as content]
            [saison.path :as path]
            [saison.proto :as proto]
            [saison.source.data :as data]
            [saison.transform.aliases :as sut]))

(deftest aliases-test
  (let [src (sut/resolve
             (data/source
              {:pathname "/index.html"
               :metadata {:mime-type "text/html"}
               :content "<a href=\"abt\">about</a> <a href=\"thr\">there</a>"}
              {:pathname "/about.html"
               :content ""
               :metadata {:alias "abt"}}
              {:pathname "/thereee"
               :content ""
               :metadata {:alias "thr"}}))
        paths (proto/scan src)
        path (path/find-by-path paths "/index.html")
        content (-> path
                    (path/content paths {})
                    content/string)]
    (is (str/index-of content "href=\"/about.html\""))
    (is (str/index-of content "href=\"/thereee\""))))
