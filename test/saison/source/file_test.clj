(ns saison.source.file-test
  (:require [clojure.test :as t :refer [deftest is]]
            [saison.path :as path]
            [saison.proto :as proto]
            [saison.source.file :as sut]))

(deftest no-base-path
  (let [source (sut/files {:root "./fixtures"})
        paths (proto/scan source)
        robots (path/find-by-path paths "/a/robots.txt")]
    (is (some? robots))))

(deftest prepends-base-path
  (let [source (sut/files {:root "./fixtures"
                           :base-path "hello"})
        paths (proto/scan source)
        robots (path/find-by-path paths "/hello/a/robots.txt")]
    (is (some? robots))))

(deftest metadata-is-supplied
  (let [source (sut/files {:root "fixtures/a"
                           :metadata {:something true}})
        paths (proto/scan source)
        robots (path/find-by-path paths "/robots.txt")]
    (is (= (:something (proto/metadata robots))
           true))))

(deftest content-reads-file
  (let [source (sut/files {:root "fixtures/"})
        site {:source source}
        paths (proto/scan source)
        robots (path/find-by-path paths "/a/robots.txt")
        content (proto/content robots paths site)]
    (is (some? content))))
