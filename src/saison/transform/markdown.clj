(ns saison.transform.markdown
  "Source and generator for basic markdown-templated files"
  (:require [markdown.core :refer [md-to-html-string-with-meta]]
            [saison.content :as content]
            [saison.path :as path]
            [saison.source :as source]
            [saison.util :as util]
            [clojure.java.io :as io])
  (:import [com.vladsch.flexmark.html HtmlRenderer]
           [com.vladsch.flexmark.parser Parser]
           [com.vladsch.flexmark.util.data MutableDataSet]
           [com.vladsch.flexmark.ext.yaml.front.matter YamlFrontMatterExtension]))

(defn ext-to-html [path]
  (util/set-path-extension (path/pathname path) "html"))

(defn parse-markdown [stream]
  (let [options (doto (MutableDataSet.)
                  (.set Parser/EXTENSIONS [(YamlFrontMatterExtension/create)]))
        parser (.build (Parser/builder options))
        renderer (.build (HtmlRenderer/builder options))
        document (with-open [rdr (io/reader stream)]
                   (.parseReader parser rdr))]
    {:html (.render renderer document)
     :metadata {}}))

(defn parse-markdown-from-path [path]
  (let [existing-meta (path/metadata path)
        markdown-content (path/content path)
        markdown-stream (content/input-stream markdown-content)
        {:keys [metadata html]} (parse-markdown markdown-stream)
        ;; md-to-meta gives us a map of keywords to arrays of strings.
        ;; (because you could supply the same header twice)
        ;; here I arbitrarily take the first value.
        new-metadata (reduce (fn [m [k v]]
                               (assoc m k (first v)))
                             (assoc existing-meta :mime-type "text/html")
                             metadata)]
    {:metadata new-metadata
     :content html}))

(defn markdown?
  [path]
  (#{"md" "markdown"} (util/path-extension (path/pathname path))))

(defn markdown-transformer []
  (path/transformer
   :name "markdown"
   :where markdown?
   :pathname ext-to-html
   :metadata-and-content parse-markdown-from-path))

(defn markdown
  "A source step that will parse any markdown path into HTML."
  []
  (source/transform-paths (markdown-transformer)))
