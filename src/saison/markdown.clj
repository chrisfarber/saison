(ns saison.markdown
  "Source and generator for basic markdown-templated files"
  (:require [net.cgrand.enlive-html :as html]
            [clojure.java.io :as io]
            [clojure.string :as str]
            [saison.util :as util]))

(defn source [source-config]
  (let [path (:path source-config)
        all-files (util/list-files path)
        markdown-files (filter (fn [[rel full]]
                                 (str/ends-with? rel ".md"))
                               all-files)]
    (map (fn [[rel full]]
           {:full-path full
            :generator 'saison.markdown/generate}) markdown-files)))

(defn generate [_ _ path]
  (io/input-stream
   (.getBytes (str "hello this is " (:full-path path)))))

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

"
Metadata
- page title
- written at
- rss link
- SEO meta?

- meta name description
- meta name twitter:image twitter:card twitter:site
- meta property og:image og:site_name og:type og:title og:description

CSS and JS links to include?



approach:
either i can have a separate file with a corresponding .edn file, or
i could have some kind of header.

one more idea is to have special html tags that enlive consumes.

"
