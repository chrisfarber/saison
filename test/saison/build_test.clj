(ns saison.build-test
  (:require
   [clojure.test :refer [deftest is]]
   [saison.source :as source]
   [saison.source.data :as data]
   [saison.build :as sut]
   [saison.tempfile :as tf]))

(defn example-site-constructor [_]
  (source/construct
   (source/start (fn [{:keys [env]}]
                   (when-let [starts (:starts env)]
                     (swap! starts inc))))
   (source/stop (fn [{:keys [env]}]
                  (when-let [stops (:stops env)]
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
        site {:env {:starts starts
                    :stops stops}
              :output-to out-path
              :constructor example-site-constructor}]
    (sut/build-site site {:publish? false})
    (is (.exists (.toFile (.resolve out "a.html"))))
    (is (not (.exists (.toFile (.resolve out "only-publishing.html")))))
    (is (= 1 @starts))
    (is (= 1 @stops))))

(deftest building-a-site-for-publishing
  (let [out (tf/make-temp-dir "out")
        out-path (-> out .toFile .getAbsolutePath)
        site {:env {}
              :output-to out-path
              :constructor example-site-constructor}]
    (sut/build-site site {:publish? true})
    (is (= "a" (slurp (.toFile (.resolve out "a.html")))))
    (is (= "pub" (slurp (.toFile (.resolve out "only-publishing.html")))))))