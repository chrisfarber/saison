(ns saison.transform.markdown
  "Source and generator for basic markdown-templated files"
  (:require [markdown.core :refer [md-to-html-string md-to-meta]]
            [saison.content :as content]
            [saison.path :as path]
            [saison.source :as source]
            [saison.util :as util]))

(defn ext-to-html [path]
  (util/set-path-extension (path/pathname path) "html"))

(defn parse-metadata [path]
  (let [metadata (path/metadata path)
        content (path/content path)
        content-str (content/string content)
        parsed-meta (md-to-meta content-str)]
     ;; md-to-meta gives us a map of keywords to arrays of strings.
      ;; (because you could supply the same header twice)
      ;; here I arbitrarily take the first value.
    (reduce (fn [m [k v]]
              (assoc m k (first v)))
            (assoc metadata :mime-type "text/html")
            parsed-meta)))

(defn parse-content [path]
  (-> path path/content content/string
      (md-to-html-string :parse-meta? true
                         :footnotes? true
                         :reference-links? true)))

(defn markdown?
  [path]
  ;; TODO use the mime type rather than path extension
  (#{"md" "markdown"} (util/path-extension (path/pathname path))))

(defn markdown-transformer []
  (path/transformer
   markdown?
   {:pathname ext-to-html
    :metadata parse-metadata
    :content parse-content}))

(defn markdown
  "A source step that will parse any markdown path into HTML."
  []
  (source/transform-paths (markdown-transformer)))
