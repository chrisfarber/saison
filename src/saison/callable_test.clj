(ns saison.callable-test
  (:require [saison.callable :as sut]
            [clojure.test :as t]))

(t/deftest invoke
  (t/is (= 42 (sut/invoke 'saison.util-test/test-fn 42)))
  (t/is (= 43 (sut/invoke 'saison.util-test/test-fn 43)))
  (t/is (= 15 (sut/invoke 'saison.util-test/test-fn2 3 7 5)))
  (t/is (= 15 (sut/invoke #'+ 3 5 7)))
  (t/is (= 15 (sut/invoke + 3 5 7))))