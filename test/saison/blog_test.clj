(ns saison.blog-test
  (:require [saison.blog :as sut]
            [clojure.test :as t]
            [saison.source.data :as data]))

(t/deftest most-recent-date-no-paths
  (t/is (nil? (sut/most-recent-date nil)))
  (t/is (nil? (sut/most-recent-date []))))

(t/deftest most-recent-date-chooses-the-most-recent
  (let [p1 (data/path {:pathname "p1"
                       :metadata {:created-at #inst "2011-09-06"
                                  :published-at #inst "2011-10-10"}})
        p2 (data/path {:pathname "p2"
                       :metadata {:created-at #inst "2015-09-06"
                                  :published-at #inst "2014-10-10"}})]
    (t/is (= #inst "2015-09-06"
             (sut/most-recent-date [p2 p1])))))
