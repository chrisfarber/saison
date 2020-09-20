(ns saison.transform.markdown
  "Source and generator for basic markdown-templated files"
  (:require [markdown.core :refer [md-to-html-string]]
            [saison.content :as content]
            [saison.path :as path]
            [saison.proto :as proto]
            [saison.source :as source]
            [saison.util :as util]))

(defn rename-path-extension [path]
  (-> path
      proto/path
      (util/set-path-extension "html")))

(defn mark-as-html [path]
  (-> path
      proto/metadata
      (assoc :mime-type "text/html")))

(defn parse-markdown [path paths site]
  (-> path
      (proto/content paths site)
      content/content->string
      md-to-html-string))

(defn map-markdown
  [source-path]
  (if (#{"md" "markdown"} (util/path-extension (proto/path source-path)))
    (path/derive-path source-path
                      {:path rename-path-extension
                       :metadata mark-as-html
                       :content parse-markdown})
    source-path))

(defn markdown
  [source]
  (source/map-paths source map-markdown))
