(ns saison.transform.markdown-test
  (:require [saison.transform.markdown :as sut]
            [clojure.test :as t :refer [deftest is]]
            [saison.site :as sn]
            [clojure.java.io :as io]
            [clojure.string :as str]
            [saison.source.data :as data]
            [saison.proto :as proto]
            [saison.path :as path]
            [saison.util :as util]))

(deftest source-test
  (let [src (sut/markdown
             (data/data-source
              {:path "/index.md"
               :data "# Hello"}))
        paths (proto/scan src)
        path (path/find-by-path paths "/index.html")
        content (-> path
                    (proto/generate paths src)
                    (util/input-stream->string))]
    (is (str/index-of content "<h1>"))
    (is (str/index-of content "Hello")))
  #_(let [paths (sn/discover-paths markdown-1)
        md (first paths)
        output (sn/compile-path markdown-1 paths md)]
    (is (str/includes? output "Hello"))
    (is (= 1 (count paths)))))

(comment
  (let [f (io/file "./fixtures/markdown1/test.md")]
    (sut/parse-file f)))

