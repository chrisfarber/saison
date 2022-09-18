(ns saison.path-test
  (:require [clojure.test :as t :refer [deftest is]]
            [saison.path :as sut]
            [saison.source.data :as data]))

(deftest derive-path-metadata
  (let [paths (data/paths {:pathname "/hello"
                           :metadata {:a true}
                           :content "hi."})
        hello (first paths)
        derived (sut/derive-path
                 hello {:metadata (fn [o] (assoc (sut/metadata o)
                                                 :b "new"))})]
    (is (= {:a true
            :b "new"}
           (sut/metadata derived)))))

(deftest resolving-paths
  (is (= "/something/somewhere.html"
         (sut/resolve (data/path {:pathname "/something/else.html"})
                      "somewhere.html")))
  (is (= "/something/somewhere.html"
         (sut/resolve (data/path {:pathname "/something/else.html"})
                      "./somewhere.html")))
  (is (= "/different/somewhere.html"
         (sut/resolve (data/path {:pathname "/something/else.html"})
                      "/different/somewhere.html")))
  (is (= "/a/b/c"
         (sut/resolve (data/path {:pathname "/a/d/g"})
                      "../b/c"))))

(deftest canonicalizing-paths
  (is (= "https://atomicobject.com/a/b/c.html"
         (sut/canonicalize "https://atomicobject.com/a"
                           "b/c.html"))))

(deftest path-info
  (let [paths (data/paths {:pathname "/hello"
                           :metadata {:a true}
                           :content "."}
                          {:pathname "/a"
                           :metadata {:something true}
                           :content "."}
                          {:pathname "b"
                           :metadata {:something false}
                           :content "."}
                          {:pathname "/hello"
                           :metadata {:b true}
                           :content "."})]
    (is (= {"/hello" {:a true}
            "b" {:something false}
            "/a" {:something true}}
           (sut/path-info paths)))))