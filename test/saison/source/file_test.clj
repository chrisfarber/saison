(ns saison.source.file-test
  (:require [saison.source.file :as sut]
            [clojure.test :as t :refer [deftest is]]
            [saison.source :as source]
            [saison.path :as path]))

(deftest no-base-path
  (let [source (sut/files {:root "./fixtures"})
        paths (source/scan source)
        robots (path/find-by-path paths "/a/robots.txt")]
    (is (some? robots))))

(deftest prepends-base-path
  (let [source (sut/files {:root "./fixtures"
                           :base-path "hello"})
        paths (source/scan source)
        robots (path/find-by-path paths "/hello/a/robots.txt")]
    (is (some? robots))))

(deftest metadata-is-supplied
  (let [source (sut/files {:root "fixtures/a"
                           :metadata {:something true}})
        paths (source/scan source)
        robots (path/find-by-path paths "/robots.txt")]
    (is (= (:something (path/metadata robots))
           true))))

(deftest generate-reads-file
  )
