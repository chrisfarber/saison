(ns saison.core-test
  (:require [saison.core :as sut]
            [clojure.test :as t]
            [clojure.spec.alpha :as s]))

(t/deftest path-specs
  (t/is (s/valid? :saison.core/path
                  {:path "/"
                   :short-name "root"
                   :generator 'saison/not-real}))
  (t/is (s/valid? :saison.core/path {:path "/hello"
                                     :short-name "something"
                                     :generator 'do/wat
                                     :data {:woah :buddy}})))

(defn test-source-1
  "A source that identifies one page with simple content"
  [source-config]

  (list {:path "/index.html"
         :generator 'saison.core-test/test-generate-1}))

(defn test-source-2
  [config]

  (map (fn [path]
         {:path path
          :generator 'saison.core-test/test-generate-1})
       (:paths config)))

(t/deftest discover-paths-simple
  (let [site {:sources [{:type 'saison.core-test/test-source-1}]}
        paths (sut/discover-paths site)
        path (first paths)]
    (t/is (= 1 (count paths)))
    (t/is (= "/index.html" (:path path)))))

(t/deftest discover-paths-compound
  (let [site {:sources [{:type 'saison.core-test/test-source-1}
                        {:type 'saison.core-test/test-source-2
                         :paths ["/alpha" "/beta"]}]}
        paths (sut/discover-paths site)
        [index alpha beta] paths]
    (t/is (= 3 (count paths)))
    (t/is (= "/index.html" (:path index)))
    (t/is (= "/alpha" (:path alpha)))
    (t/is (= "/beta" (:path beta)))))
