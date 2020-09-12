(ns saison.util-test
  (:require [clojure.test :as t :refer [deftest is]]
            [saison.util :as sut]))

(deftest list-files
  (let [paths (sut/list-files "./fixtures/a")
        relative-paths (set (remove #(.endsWith % ".DS_Store")
                                    (map first paths)))]
    (is (= relative-paths
           #{"/image.png"
             "/robots.txt"
             "/robots.txt.edn"
             "/sub/thing.html"}))))

(deftest add-path-component
  (is (= (sut/add-path-component nil nil)
         "/"))
  (is (= (sut/add-path-component nil "hello")
         "/hello"))
  (is (= (sut/add-path-component "a" "b")
         "/a/b"))
  (is (= (sut/add-path-component "/" "index.html")
         "/index.html"))
  (is (= (sut/add-path-component "/hello/there" "thing.html")
         "/hello/there/thing.html"))
  (is (= (sut/add-path-component "/thing" "/thing2")
         "/thing/thing2"))
  (is (= (sut/add-path-component "/thing/" "/thing2/")
         "/thing/thing2"))
  (is (= (sut/add-path-component "/thing/" "./what.thing/ind")
         "/thing/./what.thing/ind")))

(deftest path-extension
  (is (= (sut/path-extension "aoenuthoeu.pdf")
         "pdf"))
  (is (= (sut/path-extension "/hello/a/b/c/index.html")
         "html"))
  (is (= (sut/path-extension "/meh.what/a/b.thing.meh/thing.mD.htMl")
         "html")))

(deftest compound-path-extension
  (is (= (sut/compound-path-extension "/meh.what/a/b.thing.meh/thing.Md.HTml")
         "md.html")))

(deftest set-path-extension
  (is (= "/thing/thing1.html"
         (sut/set-path-extension "/thing/thing1.md" "html")))
  (is (= "/thing/thing1.test.html"
         (sut/set-path-extension "/thing/thing1.test.css" "html"))))

(deftest ->input-stream
  (let [data (with-open [stream (sut/->input-stream "fixtures/a/robots.txt")]
               (slurp stream))]
    (is (= data
           "hi robots\n"))))

(deftest data->input-stream
  (is (= "stuff"
         (with-open [stream (sut/data->input-stream "stuff")]
           (slurp stream)))))

(deftest input-stream->string
  (let [v "aoentuhaoenuth"]
    (= v
       (-> v
           sut/data->input-stream
           sut/input-stream->string))))
