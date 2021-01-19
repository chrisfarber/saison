(ns saison.transform.short-name-links
  (:require [net.cgrand.enlive-html :as html]
            [saison.content.html :refer [edit-html]]
            [saison.path :as path :refer [deftransform]]
            [saison.source :as source :refer [defsource]]
            [saison.util :as util]))

(deftransform apply-short-links
    []
  (content [content]
    (let [url-expansion-map (path/short-name-expansion-map path/*paths*)]
      (edit-html content
        [#{:link :a}]
        (fn [node]
          (let [href (get-in node [:attrs :href])
                resolved-href (get url-expansion-map href)]
            (if resolved-href
              (assoc-in node [:attrs :href] resolved-href)
              node)))))))

(defsource short-name-links
    [source]
  (input source)
  (map-where path/is-html? apply-short-links))
