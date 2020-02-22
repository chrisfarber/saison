(ns saison.static-test
  (:require [saison.static :as sut]
            [clojure.test :as t]))

(defn find-path [paths name]
  (first (filter #(= name (:saison.path/path %)) paths)))

(t/deftest source-test
  (let [path "./fixtures/a"
        r (sut/source {:path path})
        image-png (find-path r "/image.png")
        sub-thing (find-path r "/sub/thing.html")]
    (t/is (= 3 (count r)))
    (t/is (not (nil? sub-thing)))
    (t/is (= (:saison.path/generator sub-thing) 'saison.static/generator))))

(comment
  (sut/source {:path "src"})
  )
