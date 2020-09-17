(ns saison.transform.short-name-links
  (:require [saison.source :as source]
            [saison.util :as util]
            [saison.proto :as proto]
            [saison.path :as path]
            [net.cgrand.enlive-html :as html]
            [clojure.string :as str]))

(defn apply-short-links
  [path paths site]
  (let [oc (proto/content path paths site)
        oc-stream (util/->input-stream oc)
        parsed (html/html-resource oc-stream)
        url-expansion-map (path/short-name-expansion-map paths)]
    (util/data->input-stream
     (str/join
      (html/emit*
       (html/at parsed [#{:link :a}]
                (fn [node]
                  (let [full-href (get url-expansion-map (get-in node [:attrs :href]))]
                    (if full-href
                      (assoc-in node [:attrs :href] full-href)
                      node)))))))))

(defn map-short-links
  [source-path]
  (if (#{"htm" "html"} (util/path-extension (proto/path source-path)))
    (path/derive-path source-path
                      {:content apply-short-links})
    source-path))

(defn short-name-links
  [source]
  (source/map-paths source map-short-links))
