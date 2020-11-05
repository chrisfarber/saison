(ns saison.source-test
  (:require [clojure.test :as t :refer [deftest is]]
            [saison.proto :as proto]
            [saison.source :as sut]
            [saison.source.data :as data]
            [saison.path :as path]))

(deftest concat-sources
  (let [s1 (data/data-source
            {:path "/index.html"
             :data "index"}
            {:path "/robots.txt"
             :data "hi robots"})
        s2 (data/data-source
            {:path "/stuff.md"
             :data "stuff"})
        combined (sut/concat-sources s1 s2)
        outputs (proto/scan combined)]
    (is (= 3 (count outputs)))))

(deftest concat-sources-handles-watching
  (let [watchers-1 (volatile! 0)
        watchers-2 (volatile! 0)
        fires (volatile! 0)
        source-1 (sut/construct
                   (watch [cb]
                     (vswap! watchers-1 inc)
                     (cb)
                     (fn []
                       (vswap! watchers-1 dec))))
        source-2 (sut/construct
                   (watch [cb]
                     (vswap! watchers-2 inc)
                     (cb)
                     (fn []
                       (vswap! watchers-2 dec))))
        merged (sut/concat-sources source-1 source-2)]
    (is (zero? @watchers-1))
    (is (zero? @watchers-2))
    (is (zero? @fires))
    (let [close-fn (proto/watch merged (fn [] (vswap! fires inc)))]
      (is (= 1 @watchers-1))
      (is (= 1 @watchers-2))
      (is (= 2 @fires))
      (close-fn))
    (is (zero? @watchers-1))
    (is (zero? @watchers-2))
    (is (= 2 @fires))))
