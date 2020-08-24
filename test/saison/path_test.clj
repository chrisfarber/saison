(ns saison.path-test
  (:require saison.path
            [clojure.test :as t]
            [clojure.spec.alpha :as s]))

(t/deftest path-specs
  (let [path-a {:full-path "/"
                :short-name "root"
                :generator 'saison/not-real}
        path-b {:full-path "/hello"
                :short-name "something"
                :generator 'do/wat
                :data {:woah :buddy}}]

    (t/is (s/valid? :saison.path/path
                    path-a))
    (t/is (s/valid? :saison.path/path path-b))))
