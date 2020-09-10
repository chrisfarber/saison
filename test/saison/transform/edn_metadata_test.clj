(ns saison.transform.edn-metadata-test
  (:require [saison.transform.edn-metadata :as sut]
            [clojure.test :as t :refer [deftest is]]
            [saison.source.data :as data]
            [saison.proto :as proto]
            [saison.path :as path]))

(deftest parse-metadata-test
  (let [src (data/data-source
             {:path "/hello/index.html"
              :data ""}
             {:path "/hello/index.html.edn"
              :data (pr-str {:title "Hello"
                             :mime-type "bogus"})}
             {:path "/hello/index"
              :data ""}
             {:path "/index.html"
              :data ""})
        processed (sut/file-metadata src)
        paths (proto/scan processed)
        subject (path/find-by-path paths "/hello/index.html")
        subject-meta (proto/metadata subject)
        meta-path (path/find-by-path paths "/hello/index.html.edn")]
    (is (nil? meta-path))
    (is (= "Hello"
           (:title subject-meta)))
    (is (= "bogus"
           (:mime-type subject-meta)))))
