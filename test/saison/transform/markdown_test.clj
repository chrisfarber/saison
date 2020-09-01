(ns saison.transform.markdown-test
  (:require [saison.transform.markdown :as sut]
            [clojure.test :as t :refer [deftest is]]
            [saison.site :as sn]
            [clojure.java.io :as io]
            [clojure.string :as str]))

(def markdown-1
  {:sources [{:type 'saison.markdown/source
              :path "./fixtures/markdown1"}]})

(deftest source-test
  #_(let [paths (sn/discover-paths markdown-1)
        md (first paths)
        output (sn/compile-path markdown-1 paths md)]
    (is (str/includes? output "Hello"))
    (is (= 1 (count paths)))))

(comment
  (let [f (io/file "./fixtures/markdown1/test.md")]
    (sut/parse-file f)))

