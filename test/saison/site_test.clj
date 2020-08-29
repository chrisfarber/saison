(ns saison.site-test
  (:require [saison.site :as sut]
            [clojure.test :as t :refer [deftest is]]))

(defn test-source-1
  "A source that identifies one page with simple content"
  [_]

  (list {:path "/index.html"
         :generator 'saison.site-test/test-generate-1}))

(defn test-source-2
  [config]

  (map (fn [path]
         {:path path
          :generator 'saison.site-test/test-generate-1})
       (:paths config)))

(deftest discover-paths-simple
  (let [site {:sources [{:type 'saison.site-test/test-source-1}]}
        paths (sut/discover-paths site)
        path (first paths)]
    (is (= 1 (count paths)))
    (is (= "/index.html" (:path path)))))

(deftest discover-paths-compound
  (let [site {:sources [{:type 'saison.site-test/test-source-1}
                        {:type 'saison.site-test/test-source-2
                         :paths ["/alpha" "/beta"]}]}
        paths (sut/discover-paths site)
        [index alpha beta] paths]
    (is (= 3 (count paths)))
    (is (= "/index.html" (:path index)))
    (is (= "/alpha" (:path alpha)))
    (is (= "/beta" (:path beta)))))
