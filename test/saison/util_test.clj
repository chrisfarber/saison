(ns saison.util-test
  (:require [saison.util :as sut]
            [clojure.test :as t]))

(t/deftest list-files
  (let [paths (sut/list-files "./fixtures/a")
        relative-paths (set (map first paths))]
    (t/is (= relative-paths
             #{"/image.png"
               "/robots.txt"
               "/sub/thing.html"}))))
