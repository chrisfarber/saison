(ns saison.transform.aliases
  (:require [saison.content.html :refer [edit-html]]
            [saison.path :as path]
            [saison.source :as source]))

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

(defn resolve-aliases-in-content [path]
  (let [content (path/content path)
        url-expansion-map (alias-expansions path/*paths*)]
    (edit-html content
               [#{:link :a}]
               (fn [node]
                 (let [href (get-in node [:attrs :href])
                       resolved-href (get url-expansion-map href)]
                   (if resolved-href
                     (assoc-in node [:attrs :href] resolved-href)
                     node))))))

(def resolve-aliases-in-path
  (path/transformer
   {:content resolve-aliases-in-content}))

(defn resolve-path-aliases
  "A source step for resolving path aliases in HTML files."
  []
  (source/map-paths-where path/html? resolve-aliases-in-path))
