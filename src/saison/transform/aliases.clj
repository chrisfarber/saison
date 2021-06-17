(ns saison.transform.aliases
  (:require [clojure.tools.logging :as log]
            [saison.content.html :as htmlc]
            [saison.path :as path]
            [saison.transform.url :as url]
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
    (htmlc/edit*
     (path/content path)
     (url/edit-urls
      (fn [href]
        (or (get url-expansion-map href) href))))))

(defn resolve-transformer [expansion-map]
  (path/transformer
   :name "resolve-path-aliases"
   :where path/html?
   :content (resolve-aliases-in-content expansion-map)))

(defn resolve-aliases-in-path [paths]
  (let [expansion-map (alias-expansions paths)]
    (log/debug "found path aliases:" expansion-map)
    (resolve-transformer expansion-map)))

(defn resolve-path-aliases
  "A source step for resolving path aliases in HTML files."
  [& {:keys [aliases
             from-metadata]
      :or {from-metadata true}}]
  (source/steps
   (when aliases
     (source/transform-paths
      (resolve-transformer aliases)))
   (when from-metadata
     (source/transform-paths-contextually resolve-aliases-in-path))))
