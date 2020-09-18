(ns saison.content-test
  (:require [saison.content :as sut]
            [clojure.test :as t :refer [deftest is]]))

(deftest strings-in-and-out
  (is (= "hello"
         (-> "hello"
             sut/content->input-stream
             sut/content->string))))

(deftest idempotent-strings
  (is (= "abcd"
         (sut/content->string "abcd"))))

