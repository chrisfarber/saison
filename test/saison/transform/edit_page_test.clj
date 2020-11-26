(ns saison.transform.edit-page-test
  (:require [saison.transform.edit-page :as sut]
            [clojure.test :as t :refer [deftest is]]
            [saison.source.data :as data]
            [saison.content.html :as htmlc]
            [saison.path :as path]
            [saison.proto :as proto]
            [net.cgrand.enlive-html :as html]
            [clojure.string :as str]
            [saison.content :as content]))

(deftest editing-pages
  (let [source (data/source
                {:pathname "/thing/1.html"
                 :metadata {:title "thing 1"}
                 :content "<div><h1></h1><p>this is a page</p></div>"}
                {:pathname "/thing/2.html"
                 :metadata {:title "thing 2"}
                 :content "<div><h1></h1><p>this is a page</p></div>"})
        edited (sut/edit-page source "/thing/2.html"
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
