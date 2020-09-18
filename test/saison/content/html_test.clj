(ns saison.content.html-test
  (:require [saison.content.html :as sut]
            [clojure.test :as t :refer [deftest is]]
            [saison.content :as content]
            [net.cgrand.enlive-html :as html]))

(deftest converting-to-from-html
  (let [original "<p>hi <a>there</a></p>"
        parsed (sut/content->html original)]
    (is (= original (content/content->string parsed)))
    (is (= parsed
           (sut/content->html parsed)))
    (is (= "<p>hi <a>again</a></p>"
           (-> parsed
               (html/at [#{:a}]
                        (fn [node]
                          (assoc node :content "again")))
               sut/as-html
               content/content->string)))))

(deftest alter-html
  (let [original "<h1>title</h1>"]
    (is (= "<h1>new title</h1>"
           (content/content->string
            (sut/alter-html-content
             [nodes original]
             (html/at nodes
                      [#{:h1}]
                      (fn [node]
                        (assoc node :content "new title")))))))))

