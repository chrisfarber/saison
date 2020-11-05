(ns saison.transform.short-name-links
  (:require [net.cgrand.enlive-html :as html]
            [saison.content.html :refer [edit-html]]
            [saison.path :as path]
            [saison.source :as source :refer [defsource]]
            [saison.util :as util]))

(defn apply-short-links
  [path]
  (let [oc (path/content path)
        url-expansion-map (path/short-name-expansion-map path/*paths*)]
    (edit-html oc
      [#{:link :a}]
      (fn [node]
        (let [href (get-in node [:attrs :href])
              resolved-href (get url-expansion-map href)]
          (if resolved-href
            (assoc-in node [:attrs :href] resolved-href)
            node))))))

(defsource short-name-links
    [source]
  (input source)
  (map (fn [source-path]
         (if (#{"htm" "html"} (util/path-extension (path/pathname source-path)))
           (path/derive-path source-path
                             {:content apply-short-links})
           source-path))))
