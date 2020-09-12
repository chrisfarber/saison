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
  ;; there must be a nicer way to do this, but I am in a hurry
  (let [pathname (proto/url-path path)
        ext (util/path-extension pathname)
        ext-len (count ext)
        path-len (count pathname)
        without-ext (.substring pathname 0 (- path-len ext-len 1))]
    (str without-ext ".html")))

(defn parse-markdown [path paths site]
  #_(let [oc (proto/generate path paths site)-]))

(defn map-markdown
  [source-path]
  (if (#{"md" "markdown"} (util/path-extension (proto/url-path source-path)))
    (path/derive-path source-path {:url-path rename-path-extension
                            :generate parse-markdown})
    source-path))

(defn markdown
  [source]
  (source/map-paths source map-markdown))

#_(defn source [source-config]
  (let [path (:path source-config)
        all-files (util/list-files path)
        markdown-files (filter (fn [[rel full]]
                                 (str/ends-with? rel ".md"))
                               all-files)]
    (map (fn [[name file]]
           {:full-path name
            :data {:file file}
            :generator 'saison.markdown/generate}) markdown-files)))

#_(defn parse-markdown-file [f]
  (with-open [file-stream (util/->input-stream f)]
    (let [output (java.io.StringWriter.)]
      (md-to-html file-stream output)
      (str output))))

#_(defn generate [_ _ path]
  (let [markdown-file (get-in path [:data :file])]
    (parse-markdown-file markdown-file)))

(comment
  (def r (html/html-resource (io/as-file "./fixtures/b/index.html")))

  (html/select r [:set])
  (html/select r [:title])
  (html/at r
           [[:link (html/attr= :rel "snippet")]] (fn [n]
                                                   {:tag :p
                                                    :attrs nil
                                                    :content ["OH MY GOD ITs " (get-in n [:attrs :href])]}))

  (print (str/join (html/emit* (html/at r
                                        [:title] (fn [nd] (assoc nd :content "my title---------------------")))))))
