(ns saison.transform.markdown
  "Source and generator for basic markdown-templated files"
  (:require [markdown.core :refer [md-to-html-string-with-meta]]
            [saison.content :as content]
            [saison.path :as path]
            [saison.source :as source]
            [saison.util :as util]))

(defn ext-to-html [path]
  (util/set-path-extension (path/pathname path) "html"))

(defn parse-markdown [path]
  (let [existing-meta (path/metadata path)
        markdown-str (-> path path/content content/string)
        {:keys [metadata html]} (md-to-html-string-with-meta
                                 markdown-str
                                 :parse-meta? true
                                 :footnotes? true
                                 :reference-links? true)
        ;; md-to-meta gives us a map of keywords to arrays of strings.
        ;; (because you could supply the same header twice)
        ;; here I arbitrarily take the first value.
        new-metadata (reduce (fn [m [k v]]
                               (assoc m k (first v)))
                             (assoc existing-meta :mime-type "text/html")
                             metadata)]
    [new-metadata html]))

(defn markdown?
  [path]
  ;; TODO use the mime type rather than path extension
  (#{"md" "markdown"} (util/path-extension (path/pathname path))))

(defn markdown-transformer []
  (path/transformer
   :name "markdown"
   :where markdown?
   :pathname ext-to-html
   :metadata-and-content parse-markdown))

(defn markdown
  "A source step that will parse any markdown path into HTML."
  []
  (source/transform-paths (markdown-transformer)))
