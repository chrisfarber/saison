(ns saison.transform.markdown
  "Source and generator for basic markdown-templated files"
  (:require [markdown.core :refer [md-to-html-string md-to-meta]]
            [saison.content :as content]
            [saison.path :as path]
            [saison.source :as source :refer [defsource]]
            [saison.util :as util]))

(path/deftransform parse-markdown
    []

  (path [pathname]
    (util/set-path-extension pathname "html"))

  ;; unfortunately we end up consuming the content twice;
  ;; once for metadata and once for content.
  ;; i have ideas for how to optimize that eventually, if need be
  (metadata [metadata content]
    (let [content-str (content/string content)
          parsed-meta (md-to-meta content-str)]
      ;; md-to-meta gives us a map of keywords to arrays of strings.
      ;; (because you could supply the same header twice)
      ;; here I arbitrarily take the first value.
      (reduce (fn [m [k v]]
                (assoc m k (first v)))
              (assoc metadata :mime-type "text/html")
              parsed-meta)))

  (content [content]
    (md-to-html-string (content/string content) :parse-meta? true)))

(defn is-markdown?
  [path]
  (#{"md" "markdown"} (util/path-extension (path/pathname path))))

(defsource markdown
    [source]
  (input source)
  (map-where is-markdown? parse-markdown))
