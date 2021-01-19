(ns saison.transform.edit-test
  (:require [clojure.string :as str]
            [clojure.test :as t :refer [deftest is]]
            [net.cgrand.enlive-html :as html]
            [saison.content :as content]
            [saison.content.html :as htmlc]
            [saison.path :as path]
            [saison.proto :as proto]
            [saison.source.data :as data]
            [saison.transform.edit :as sut]))

(deftest editing-pages
  (let [source (data/source
                {:pathname "/thing/1.html"
                 :metadata {:title "thing 1"}
                 :content "<div><h1></h1><p>this is a page</p></div>"}
                {:pathname "/thing/2.html"
                 :metadata {:title "thing 2"}
                 :content "<div><h1></h1><p>this is a page</p></div>"})
        edited (sut/edit-path source "/thing/2.html"
                 [path]
                 (let [titles (map #(-> % path/metadata :title) path/*paths*)]
                   (htmlc/edits
                    [:h1] (html/clone-for [title titles]
                                          [:h1] (html/content title)))))
        paths (proto/scan edited)
        path (path/find-by-path paths "/thing/2.html")
        content (content/string (path/content path paths {}))]
    (is (str/index-of content "<h1>thing 1</h1>"))
    (is (str/index-of content "<h1>thing 2</h1>"))))
