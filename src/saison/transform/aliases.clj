(ns saison.transform.aliases
  (:refer-clojure :exclude [resolve alias])
  (:require [saison.content.html :refer [edit-html]]
            [saison.path :as path :refer [deftransform]]
            [saison.source :as source :refer [defsource]]))

(defn alias-expansions
  [paths]
  (reduce (fn [expansions path]
            (let [alias (-> path
                            path/metadata
                            :alias)]
              (if alias
                (assoc expansions alias (path/pathname path))
                expansions)))
          {}
          paths))

(deftransform aliases
    []
  (content [content]
    (let [url-expansion-map (alias-expansions path/*paths*)]
      (edit-html content
        [#{:link :a}]
        (fn [node]
          (let [href (get-in node [:attrs :href])
                resolved-href (get url-expansion-map href)]
            (if resolved-href
              (assoc-in node [:attrs :href] resolved-href)
              node)))))))

(defsource resolve
    [source]
  (input source)
  (map-where path/is-html? aliases))
