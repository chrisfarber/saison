(ns saison.static-test
  (:require [saison.static :as sut]
            [clojure.test :as t]
            [clojure.spec.alpha :as s]))

(defn find-path [paths name]
  (first (filter #(= name (:full-path %)) paths)))

(t/deftest source-test
  (let [path "./fixtures/a"
        results (sut/source {:path path})
        image-png (find-path results "/image.png")
        sub-thing (find-path results "/sub/thing.html")]
    (t/is (= 3 (count results)))
    (t/is (not (nil? sub-thing)))
    (t/is (not (nil? image-png)))
    (t/is (= (:generator sub-thing) 'saison.static/generate))
    (t/is (s/valid? (s/* :saison.path/path) results))))

(comment
  (sut/source {:path "./fixtures/a"}))
