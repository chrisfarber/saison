(ns saison.transform.markdown
  "Source and generator for basic markdown-templated files"
  (:require [clojure.java.io :as io]
            [clojure.string :as str]
            [markdown.core :refer [md-to-html md-to-html-string md-to-meta]]
            [saison.util :as util]
            [saison.source :as source]
            [saison.proto :as proto]
            [saison.path :as path]))

(defn rename-path-extension [path]
  (-> path
      proto/url-path
      (util/set-path-extension "html")))

(defn parse-markdown [path paths site]
  (let [oc (proto/generate path paths site)
        oc-stream (util/->input-stream oc)
        oc-string (util/input-stream->string oc-stream)]
    (util/data->input-stream (md-to-html-string oc-string))))

(defn map-markdown
  [source-path]
  (if (#{"md" "markdown"} (util/path-extension (proto/url-path source-path)))
    (path/derive-path source-path {:url-path rename-path-extension
                            :generate parse-markdown})
    source-path))

(defn markdown
  [source]
  (source/map-paths source map-markdown))
