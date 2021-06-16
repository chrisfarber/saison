(ns saison.content.html-test
  (:require [clojure.test :as t :refer [deftest is]]
            [net.cgrand.enlive-html :as html]
            [saison.content :as content]
            [saison.content.html :as sut]))

(deftest converting-to-from-html
  (let [original "<p>hi <a>there</a></p>"
        parsed (sut/html original)]
    (is (= original (content/string parsed)))
    (is (= parsed
           (sut/html parsed)))
    (is (= "<p>hi <a>again</a></p>"
           (-> parsed
               (html/at [#{:a}]
                        (fn [node]
                          (assoc node :content "again")))
               sut/as-html
               content/string)))))

(deftest editing-html
  (let [edited (content/string
                (sut/edit*
                 "<title>hello</title><p></p>"
                 (sut/edits [:title] (html/content "okay"))
                 [(sut/edits [:p] (html/content "there"))]))]
    (is (= "<title>okay</title><p>there</p>"
           edited))))
