(ns saison.source.file-test
  (:require [saison.source.file :as sut]
            [clojure.test :as t :refer [deftest is]]
            [saison.source :as source]
            [saison.path :as path]
            [saison.proto :as proto]))

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

(deftest generate-reads-file
  )
