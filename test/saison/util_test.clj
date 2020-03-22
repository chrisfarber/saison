(ns saison.util-test
  (:require [saison.util :as sut]
            [clojure.test :as t]))

(t/deftest list-files
  (let [paths (sut/list-files "./fixtures/a")
        relative-paths (set (map first paths))]
    (t/is (= (disj relative-paths "/.DS_Store")
             #{"/image.png"
               "/robots.txt"
               "/sub/thing.html"}))))

(t/deftest add-path-component
  (t/is (= (sut/add-path-component "/" "index.html")
           "/index.html"))
  (t/is (= (sut/add-path-component "/hello/there" "thing.html")
           "/hello/there/thing.html")))
