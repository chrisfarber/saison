(ns saison.transform.short-name-links-test
  (:require [saison.transform.short-name-links :as sut]
            [clojure.test :as t :refer [deftest is]]
            [saison.source.data :as data]
            [saison.proto :as proto]
            [saison.path :as path]
            [saison.util :as util]
            [clojure.string :as str]))

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
                    (proto/content paths src)
                    (util/input-stream->string))]
    (is (str/index-of content "href=\"/about.html\""))
    (is (str/index-of content "href=\"/thereee\""))))
