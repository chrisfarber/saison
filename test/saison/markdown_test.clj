(ns saison.markdown-test
  (:require [saison.markdown :as sut]
            [clojure.test :as t]
            [saison.site :as sn]
            [clojure.java.io :as io]))

(def markdown-1
  {:sources [{:type 'saison.markdown/source
              :path "./fixtures/markdown1"}]})

(t/deftest source-test
  (let [paths (sn/discover-paths markdown-1)
        md (first paths)
        output (sn/compile-path markdown-1 paths md)
        output-str (with-open [rdr (io/reader output)]
                     (slurp rdr))]
    (t/is (= 4 output-str))
    (t/is (= 1 (count paths)))))

(with-open [rdr (io/reader (sut/generate nil nil {:full-path "hello"}))]
  (doall (line-seq rdr)))
