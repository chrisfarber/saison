(ns saison.transform.markdown
  "Source and generator for basic markdown-templated files"
  (:require [markdown.core :refer [md-to-html-string]]
            [saison.content :as content]
            [saison.path :as path]
            [saison.source :as source]
            [saison.util :as util]))

(path/deftransform parse-markdown
  []

  (path [path]
        (util/set-path-extension path "html"))

  (metadata [metadata]
            (assoc metadata :mime-type "text/html"))

  (content [content metadata]
           (md-to-html-string (content/content->string content))))

(defn markdown
  [source]
  (source/map-paths-where
   source
   #(#{"md" "markdown"} (util/path-extension (path/path->name %)))
   parse-markdown))

