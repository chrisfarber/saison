(ns saison.source.file-test
  (:require [clojure.test :as t :refer [deftest is]]
            [saison.path :as path]
            [saison.proto :as proto]
            [saison.source.file :as sut]
            [saison.tempfile :refer [make-temp-dir]]))

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
    (is (= (:something (path/metadata robots))
           true))))

(deftest metadata-is-read-from-associated-files
  (let [source (sut/files {:root "fixtures/a"
                           :read-metadata-files true})
        paths (proto/scan source)
        robots (path/find-by-path paths "/robots.txt")]
    (is (= "robots?"
           ;; this title should come from the .meta.edn file
           (:title (path/metadata robots))))))

(deftest metadata-from-associated-files-can-be-disabled
  (let [source (sut/files {:root "fixtures/a"
                           :parse-metadata false})
        paths (proto/scan source)
        robots (path/find-by-path paths "/robots.txt")]
    (is (nil?
         (:title (path/metadata robots))))))

(deftest content-reads-file
  (let [source (sut/files {:root "fixtures/"})
        paths (proto/scan source)
        robots (path/find-by-path paths "/a/robots.txt")
        content (path/content robots)]
    (is (some? content))))

(deftest changed-files-are-not-identical
  (let [dir (make-temp-dir "changed-files-are-not-equal")
        source (sut/files {:root (str dir)})
        temp (.resolve dir "test.html")
        write #(spit (.toFile temp) %)]
    (write "a")
    (proto/start source {})
    (let [path (-> source proto/scan first)]
      (proto/stop source {})
      (write "b")
      (proto/start source {})
      (let [path' (-> source proto/scan first)]
        (is (not (identical? path path')))))))
