(ns saison.content-test
  (:require [clojure.test :as t :refer [deftest is]]
            [saison.content :as sut]))

(deftest strings-in-and-out
  (is (= "hello"
         (-> "hello"
             sut/input-stream
             sut/string))))

(deftest idempotent-strings
  (is (= "abcd"
         (sut/string "abcd"))))

