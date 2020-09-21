(ns saison.transform.short-name-links
  (:require [net.cgrand.enlive-html :as html]
            [saison.content.html :refer [alter-html-content]]
            [saison.path :as path]
            [saison.source :as source]
            [saison.util :as util]))

(defn apply-short-links
  [path]
  (let [oc (path/path->content path)
        url-expansion-map (path/short-name-expansion-map path/*paths*)]
    (alter-html-content
     [html oc]
     (html/at html [#{:link :a}]
              (fn [node]
                (let [href (get-in node [:attrs :href])
                      resolved-href (get url-expansion-map href)]
                  (if resolved-href
                    (assoc-in node [:attrs :href] resolved-href)
                    node)))))))

(defn map-short-links
  [source-path]
  (if (#{"htm" "html"} (util/path-extension (path/path->name source-path)))
    (path/derive-path source-path
                      {:content apply-short-links})
    source-path))

(defn short-name-links
  [source]
  (source/map-paths source map-short-links))
