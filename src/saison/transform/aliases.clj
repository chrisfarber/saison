(ns saison.transform.aliases
  (:require [saison.content.html :refer [edit-html]]
            [saison.path :as path]
            [saison.source :as source]))

(defn alias-expansions
  [paths]
  (reduce (fn [expansions path]
            (if-let [alias (-> path path/metadata :alias)]
              (assoc expansions alias (path/pathname path))
              expansions))
          {}
          paths))

(defn resolve-aliases-in-content [url-expansion-map]
  (fn [path]
    (let [content (path/content path)]
      (edit-html content
                 [#{:link :a}]
                 (fn [node]
                   (let [href (get-in node [:attrs :href])
                         resolved-href (get url-expansion-map href)]
                     (if resolved-href
                       (assoc-in node [:attrs :href] resolved-href)
                       node)))))))

(defn resolve-aliases-in-path [paths]
  (let [expansion-map (alias-expansions paths)]
    (path/transformer
     :where path/html?
     :content (resolve-aliases-in-content expansion-map))))

(defn resolve-path-aliases
  "A source step for resolving path aliases in HTML files."
  []
  (source/transform-paths-contextually resolve-aliases-in-path))
