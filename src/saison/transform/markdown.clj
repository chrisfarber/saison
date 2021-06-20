(ns saison.transform.markdown
  "Source and generator for basic markdown-templated files"
  (:require [saison.content :as content]
            [saison.path :as path]
            [saison.source :as source]
            [saison.util :as util])
  (:import [com.vladsch.flexmark.ext.tables TablesExtension]
           [com.vladsch.flexmark.html HtmlRenderer]
           [com.vladsch.flexmark.parser Parser]
           [com.vladsch.flexmark.util.data MutableDataSet]))

(defn ext-to-html [path]
  (util/set-path-extension (path/pathname path) "html"))

(defn parse-markdown [str]
  (let [options (doto (MutableDataSet.)
                  (.set TablesExtension/COLUMN_SPANS false)
                  (.set TablesExtension/APPEND_MISSING_COLUMNS true)
                  (.set TablesExtension/DISCARD_EXTRA_COLUMNS true)
                  (.set TablesExtension/HEADER_SEPARATOR_COLUMN_MATCH true)
                  (.set Parser/EXTENSIONS [(TablesExtension/create)]))
        parser (.build (Parser/builder options))
        renderer (.build (HtmlRenderer/builder options))
        document (.parse parser str)]
    (.render renderer document)))

(defn update-mime [path]
  (assoc (path/metadata path)
         :mime-type "text/html"))

(defn parse-markdown-from-path [path]
  (let [markdown-content (path/content path)
        markdown-string (content/string markdown-content)
        html (parse-markdown markdown-string)]
    html))

(defn markdown?
  [path]
  (#{"md" "markdown"} (util/path-extension (path/pathname path))))

(defn markdown-transformer []
  (path/transformer
   :name "markdown"
   :where markdown?
   :pathname ext-to-html
   :metadata update-mime
   :content parse-markdown-from-path))

(defn markdown
  "A source step that will parse any markdown path into HTML."
  []
  (source/transform-paths (markdown-transformer)))
