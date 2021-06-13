(ns saison.caching-path-test
  (:require [saison.caching-path :as sut]
            [clojure.test :refer [deftest is]]
            [saison.content.html :as htmlc]
            [clojure.java.io :as io]
            [saison.source.data :as data]
            [saison.path :as path]
            [saison.content :as content]))

(deftest round-trip-caching
  (is (= 42
         (sut/cache-to-content 42)))
  (let [html (htmlc/html "<div>hi</div>")]
    (is (= html
           (sut/content-to-cache html)))
    (is (= html
           (sut/cache-to-content html)))))

(deftest files-arent-cached
  (is (nil? (sut/content-to-cache (io/file "Readme.md"))))
  (let [p (sut/cached (data/path {:pathname "Readme.md"
                                  :content (io/file "Readme.md")}))]
    (is (some? (content/string (path/content p))))))

(deftest content-to-cache-give-bytes-for-stream
  (is (bytes? (sut/content-to-cache (content/input-stream "hi")))))

(deftest streams-are-cached
  (let [content-val (atom "hi")
        path (data/path {:pathname "/hello.txt"
                         :content #(content/input-stream @content-val)})
        cached (sut/cached path)]
    (is (= "hi" (content/string (path/content path))))
    (is (= "hi" (content/string (path/content cached))))
    (reset! content-val "different")
    (is (= "different" (content/string (path/content path))))
    (is (= "hi" (content/string (path/content cached))))))