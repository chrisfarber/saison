(ns saison.transform.edn-metadata-test
  (:require [clojure.test :as t :refer [deftest is]]
            [saison.path :as path]
            [saison.proto :as proto]
            [saison.source.data :as data]
            [saison.transform.edn-metadata :as sut]))

(deftest parse-metadata-test
  (let [src (data/data-source
             {:path "/hello/index.html"
              :data ""
              :metadata {:cool true}}
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
        subject-meta (path/path->metadata subject)
        meta-path (path/find-by-path paths "/hello/index.html.edn")]
    ;; the metadata file should not be output:
    (is (nil? meta-path))
    ;; but its contents should be put in its target path meta:
    (is (= "Hello"
           (:title subject-meta)))
    (is (= "bogus"
           (:mime-type subject-meta)))
    ;; and its original metadata should be retained:
    (is (true? (:cool subject-meta)))))
