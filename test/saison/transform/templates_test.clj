(ns saison.transform.templates-test
  (:require [clojure.string :as str]
            [clojure.test :as t :refer [deftest is]]
            [saison.content :as content]
            [saison.path :as path]
            [saison.proto :as proto]
            [saison.source :as source]
            [saison.source.data :as data]
            [saison.transform.templates :as sut]
            [saison.content.html :as htmlc]))

(deftest new-templating-test
  (let [source (source/construct
                (data/source
                 {:pathname "/hello.html"
                  :metadata {:mime-type "text/html"
                             :title "fancy title"
                             :template "test1"
                             :html-meta {:abcd "efg"}}
                  :content "<h1>heading</h1><p>content</p>"})
                (sut/templates
                 {:file "fixtures/template/template.html"
                  :name "test1"
                  :content-selector :div#content
                  :edits [sut/apply-html-metadata
                          sut/set-title]}))
        paths (proto/scan source)
        path (first paths)
        content (content/string path)]
    (is (str/index-of content "<meta charset=\"utf-8\" />"))
    (is (str/index-of content "<meta name=\"abcd\" content=\"efg\" />"))
    (is (str/index-of content "<title>fancy title"))
    (is (str/index-of content "<p>content</p"))
    (is (nil? (str/index-of content "id=\"content\"")))))

(defn run-edits [edit-fn path]
  (let [eds (edit-fn path)
        html (htmlc/html path)]
    (htmlc/as-html (htmlc/apply-edits html eds))))

(def simple-page
  "
<!DOCTYPE html>
<html>
<head>
<meta charset=\"utf-8\">
<title>hi</title>
</head>
<body>
<p>nothing to see</p>
</body>
</html")

(deftest apply-html-metadata
  (let [output (run-edits sut/apply-html-metadata
                          (data/path {:pathname "eh"
                                      :metadata {:html-meta {:hello "true"}}
                                      :content simple-page}))
        meta-tags (htmlc/select output [:html :head :meta])
        find-meta (fn [attrs]
                    (some #(and (= (:tag %) :meta)
                                (= (:attrs %) attrs))
                          meta-tags))]
    (is (find-meta {:charset "utf-8"}))
    (is (find-meta {:name "hello" :content "true"}))))
