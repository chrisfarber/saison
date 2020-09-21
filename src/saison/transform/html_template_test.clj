(ns saison.transform.html-template-test
  (:require [saison.transform.html-template :as sut]
            [clojure.test :as t :refer [deftest is]]
            [net.cgrand.enlive-html :as html]
            [clojure.java.io :as io]
            [clojure.string :as str]
            [saison.proto :as proto]
            [saison.source.data :as data]
            [saison.path :as path]
            [saison.content :as content]))

(html/deftemplate standard-template* (io/as-file "fixtures/template/template.html")
  [title meta-tags content]
  [:head [:meta html/first-of-type]] (html/clone-for [[prop value] meta-tags]
                                                     [:meta] (html/set-attr :name prop
                                                                       :content value))
  [:head :title] (html/content title)
  [:div#content] (html/substitute content))


(defn standard-template
  [content meta]
  (let [title (:title meta)
        html-meta (:html-meta-tags meta)]
    (standard-template* title html-meta content)))

(comment
  (standard-template* "eh" {} (html/html-snippet "<a>eu</a>")))

(deftest templating-test
  (let [source (sut/template-using
                (data/data-source
                 {:path "/hello.html"
                  :metadata {:mime-type "text/html"
                             :html-meta-tags {"abcd" "efg"}}
                  :data "<h1>heading</h1><p>content</p>"})
                standard-template)
        paths (proto/scan source)
        path (first paths)
        raw-content (path/path->content path paths {})
        content (content/content->string raw-content)]
    (is (str/index-of content "<meta name=\"abcd\" content=\"efg\""))))
