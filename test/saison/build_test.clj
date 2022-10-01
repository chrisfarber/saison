(ns saison.build-test
  (:require
   [clojure.test :refer [deftest is]]
   [saison.source :as source]
   [saison.source.data :as data]
   [saison.build :as sut]
   [saison.tempfile :as tf]))

(def example-source
  (source/construct
   (source/on-start (fn []
                      (when-let [starts (:starts source/*env*)]
                        (swap! starts inc))))
   (source/on-stop (fn []
                     (when-let [stops (:stops source/*env*)]
                       (swap! stops inc))))
   (data/source {:pathname "/a.html"
                 :content "a"})
   (source/when-publishing
    (data/source {:pathname "/only-publishing.html"
                  :content "pub"}))))

(deftest building-a-site-starts-and-stops
  (let [out (tf/make-temp-dir "out")
        out-path (-> out .toFile .getAbsolutePath)
        starts (atom 0)
        stops (atom 0)
        site (source/construct
              (source/set-env :starts starts)
              (source/set-env :stops stops)
              example-source)]
    (sut/build-site site {:publish? false
                          :output-to out-path})
    (is (.exists (.toFile (.resolve out "a.html"))))
    (is (not (.exists (.toFile (.resolve out "only-publishing.html")))))
    (is (= 1 @starts))
    (is (= 1 @stops))))

(deftest building-a-site-for-publishing
  (let [out (tf/make-temp-dir "out")
        out-path (-> out .toFile .getAbsolutePath)]
    (sut/build-site example-source {:publish? true
                                    :output-to out-path})
    (is (= "a" (slurp (.toFile (.resolve out "a.html")))))
    (is (= "pub" (slurp (.toFile (.resolve out "only-publishing.html")))))))
