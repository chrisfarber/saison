(ns saison.source-test
  (:require [clojure.test :as t :refer [deftest is]]
            [saison.proto :as proto]
            [saison.source :as sut]
            [saison.source.data :as data]))

(deftest concat-sources
  (let [src (sut/construct
             (data/source {:pathname "/index.html"
                           :content "index"}
                          {:pathname "/robots.txt"
                           :content "hi robots"})
             (data/source {:pathname "/stuff.md"
                           :content "stuff"}))
        outputs (proto/scan src)]
    (is (= 3 (count outputs)))))

(deftest concat-sources-handles-watching
  (let [watchers-1 (volatile! 0)
        watchers-2 (volatile! 0)
        fires (volatile! 0)
        source-1 (sut/construct
                  (sut/add-watcher (fn [cb]
                                     (vswap! watchers-1 inc)
                                     (cb)
                                     (fn []
                                       (vswap! watchers-1 dec)))))
        source-2 (sut/construct
                  (sut/add-watcher (fn [cb]
                                     (vswap! watchers-2 inc)
                                     (cb)
                                     (fn []
                                       (vswap! watchers-2 dec)))))
        merged (sut/construct source-1 source-2)]
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

(deftest source-inputs-hooks
  (let [build-count (volatile! 0)
        publish-count (volatile! 0)
        s1 (sut/construct
            (sut/before-build (fn [env]
                                (vswap! build-count inc))))
        s2 (sut/construct
            (sut/before-publish (fn [env]
                                  (vswap! publish-count inc))))
        s1-and-s2 (list s1 s2)
        merged (sut/construct
                (sut/from s1-and-s2))]
    (proto/before-build-hook merged {})
    (is (= 1 @build-count))
    (is (zero? @publish-count))
    (proto/before-publish-hook merged {})
    (is (= 1 @build-count))
    (is (= 1 @publish-count))))
