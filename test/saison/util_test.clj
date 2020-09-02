(ns saison.util-test
  (:require [saison.util :as sut]
            [clojure.test :as t :refer [deftest is]]))

(deftest list-files
  (let [paths (sut/list-files "./fixtures/a")
        relative-paths (set (remove #(.endsWith % ".DS_Store")
                                    (map first paths)))]
    (is (= relative-paths
           #{"/image.png"
             "/robots.txt"
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
