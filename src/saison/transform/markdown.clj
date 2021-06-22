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

(defn build-default-parser-options []
  (doto (MutableDataSet.)
    (.set TablesExtension/COLUMN_SPANS false)
    (.set TablesExtension/APPEND_MISSING_COLUMNS true)
    (.set TablesExtension/DISCARD_EXTRA_COLUMNS true)
    (.set TablesExtension/HEADER_SEPARATOR_COLUMN_MATCH true)
    (.set Parser/EXTENSIONS [(TablesExtension/create)])))

(defn markdown-parser [build-parser-options]
  (let [options (build-parser-options)
        parser (.build (Parser/builder options))
        renderer (.build (HtmlRenderer/builder options))]
    (fn [str]
      (let [document (.parse parser str)]
        (.render renderer document)))))

(defn update-mime [path]
  (assoc (path/metadata path)
         :mime-type "text/html"))

(defn parse-markdown-from-path
  [build-parser-options]
  (let [parser (markdown-parser build-parser-options)]
    (fn [path]
      (let [markdown-string (content/string path)]
        (parser markdown-string)))))

(defn markdown?
  [path]
  (#{"md" "markdown"} (util/path-extension (path/pathname path))))

(defn markdown-transformer [build-parser-options]
  (path/transformer
   :name "markdown"
   :where markdown?
   :pathname ext-to-html
   :metadata update-mime
   :content (parse-markdown-from-path build-parser-options)))

(defn markdown
  "A source step that will parse any markdown path into HTML."
  [& {:keys [build-parser-options]
      :or {build-parser-options build-default-parser-options}}]
  (source/transform-paths (markdown-transformer build-parser-options)))
