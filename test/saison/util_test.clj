(ns saison.util-test
  (:require [saison.util :as sut]
            [clojure.test :as t]))

(t/deftest list-files
  (let [paths (sut/list-files "./fixtures/a")
        relative-paths (set (remove #(.endsWith % ".DS_Store")
                                    (map first paths)))]
    (t/is (= relative-paths
             #{"/image.png"
               "/robots.txt"
               "/sub/thing.html"}))))

(t/deftest add-path-component
  (t/is (= (sut/add-path-component "/" "index.html")
           "/index.html"))
  (t/is (= (sut/add-path-component "/hello/there" "thing.html")
           "/hello/there/thing.html"))
  (t/is (= (sut/add-path-component "/thing" "/thing2")
           "/thing/thing2"))
  (t/is (= (sut/add-path-component "/thing/" "/thing2/")
           "/thing/thing2"))
  (t/is (= (sut/add-path-component "/thing/" "./what.thing/ind")
           "/thing/./what.thing/ind")))

(def test-fn identity)
(defn test-fn2
  [a b c]
  (+ a b c))

(t/deftest invoke
  (t/is (= 42 (sut/invoke 'saison.util-test/test-fn 42)))
  (t/is (= 43 (sut/invoke 'saison.util-test/test-fn 43)))
  (t/is (= 15 (sut/invoke 'saison.util-test/test-fn2 3 7 5)))
  (t/is (= 15 (sut/invoke #'+ 3 5 7)))
  (t/is (= 15 (sut/invoke + 3 5 7))))
