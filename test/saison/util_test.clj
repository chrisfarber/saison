(ns saison.util-test
  (:require [clojure.test :as t :refer [deftest is]]
            [saison.util :as sut]))

(deftest list-files
  (let [paths (sut/list-files "./fixtures/a")
        relative-paths (set (remove #(.endsWith % ".DS_Store")
                                    (map first paths)))]
    (is (= relative-paths
           #{"/image.png"
             "/robots.txt"
             "/robots.txt.meta.edn"
             "/sub/thing.html"}))))

(deftest add-path-component
  (is (= (sut/add-path-component nil nil)
         "/"))
  (is (= (sut/add-path-component nil "hello")
         "/hello"))
  (is (= (sut/add-path-component "a" "b")
         "/a/b"))
  (is (= (sut/add-path-component "/" "index.html")
         "/index.html"))
  (is (= (sut/add-path-component "/hello/there" "thing.html")
         "/hello/there/thing.html"))
  (is (= (sut/add-path-component "/thing" "/thing2")
         "/thing/thing2"))
  (is (= (sut/add-path-component "/thing/" "/thing2/")
         "/thing/thing2"))
  (is (= (sut/add-path-component "/thing/" "./what.thing/ind")
         "/thing/./what.thing/ind")))

(deftest path-extension
  (is (= "pdf"
         (sut/path-extension "aoenuthoeu.pdf")))
  (is (= "html"
         (sut/path-extension "/hello/a/b/c/index.html")))
  (is (= "html"
         (sut/path-extension "/meh.what/a/b.thing.meh/thing.mD.htMl")))
  (is (= nil
         (sut/path-extension "/hello/there"))))

(deftest compound-path-extension
  (is (= (sut/compound-path-extension "/meh.what/a/b.thing.meh/thing.Md.HTml")
         "md.html")))

(deftest set-path-extension
  (is (= "/thing/thing1.html"
         (sut/set-path-extension "/thing/thing1.md" "html")))
  (is (= "/thing/thing1.test.html"
         (sut/set-path-extension "/thing/thing1.test.css" "html"))))

(deftest append-url-component
  (is (= "https://chrisfarber.net/a/b/c.txt"
         (sut/append-url-component "https://chrisfarber.net/a" "/b/c.txt")))
  (is (= "https://chrisfarber.net/index.html"
         (sut/append-url-component (java.net.URL. "https://chrisfarber.net") "index.html"))))
