(ns saison.content.html-test
  (:require [clojure.test :as t :refer [deftest is]]
            [net.cgrand.enlive-html :as html]
            [saison.content :as content]
            [saison.content.html :as sut]))

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

(deftest editing-html
  (let [edited (content/content->string
                (sut/edit-html*
                 "<title>hello</title><p></p>"
                 (sut/edits [:title] (html/content "okay"))
                 [(sut/edits [:p] (html/content "there"))]))]
    (is (= "<title>okay</title><p>there</p>"
           edited))))
