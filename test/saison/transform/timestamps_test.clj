(ns saison.transform.timestamps-test
  (:require [clojure.test :as t :refer [deftest is]]
            [saison.path :as path]
            [saison.proto :as proto]
            [saison.source :as source]
            [saison.source.data :as data]
            [saison.transform.timestamps :as sut]
            [tick.alpha.api :as tick]))

(defmacro with-tempfile
  [[binding name] & forms]
  `(let [temp# (java.io.File/createTempFile ~name ".temp")
         ~binding (.getPath temp#)
         r# (do ~@forms)]
     (.delete temp#)
     r#))

(deftest timestamps-created-from-scratch
  (with-tempfile [t "temp"]
    (let [input (data/source
                 {:pathname "/hello.txt"
                  :content "hi"})
          ts (sut/timestamp-database input t)]
      (proto/before-build-hook ts {})
      (let [paths (proto/scan ts)
            path (first paths)
            pathname (path/pathname path)
            meta (path/metadata path paths {})
            db (sut/read-db t)]
        (is (some? (get-in db ["/hello.txt" :created-at])))
        (is (= "/hello.txt"
               pathname))
        (is (some? (:created-at meta)))))))

(deftest timestamps-left-in-place
  (with-tempfile [t "temp"]
    (let [previous-date (tick/- (tick/zoned-date-time) (tick/new-period 3 :days))]
      (sut/write-db t {"/hello.txt" {:created-at previous-date}})
      (let [input (data/source
                   {:pathname "/hello.txt"
                    :content "hi"})
            ts (sut/timestamp-database input t)]
        (proto/before-build-hook ts {})
        (let [paths (proto/scan ts)
              path (first paths)
              pathname (path/pathname path)
              meta (path/metadata path paths {})
              db (sut/read-db t)]
          (is (= previous-date
                 (get-in db ["/hello.txt" :created-at]))))))))
