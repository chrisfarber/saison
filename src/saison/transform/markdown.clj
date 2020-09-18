(ns saison.transform.markdown
  "Source and generator for basic markdown-templated files"
  (:require [markdown.core :refer [md-to-html-string]]
            [saison.path :as path]
            [saison.proto :as proto]
            [saison.source :as source]
            [saison.util :as util]
            [saison.content :as content]))

(defn rename-path-extension [path]
  (-> path
      proto/path
      (util/set-path-extension "html")))

(defn parse-markdown [path paths site]
  (let [oc (proto/content path paths site)
        oc-string (content/content->string oc)]
    (md-to-html-string oc-string)))

(defn map-markdown
  [source-path]
  (if (#{"md" "markdown"} (util/path-extension (proto/path source-path)))
    (path/derive-path source-path
                      {:path rename-path-extension
                       :content parse-markdown})
    source-path))

(defn markdown
  [source]
  (source/map-paths source map-markdown))
