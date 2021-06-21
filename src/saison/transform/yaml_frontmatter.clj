(ns saison.transform.yaml-frontmatter
  (:require [saison.source :as source]
            [saison.path :as path]
            [saison.frontmatter :as fm]
            [clj-yaml.core :as yaml]
            [saison.content :as content]))

(defn parse-yaml-frontmatter
  [path]
  (with-open [stream (content/input-stream (path/content path))]
    (let [meta-str (fm/frontmatter stream)
          meta (yaml/parse-string meta-str)]
      (merge (path/metadata path)
             meta))))

(defn skip-yaml-frontmatter
  [path]
  (with-open [stream (content/input-stream (path/content path))]
    (fm/skip-frontmatter stream)))

(defn yaml-frontmatter-transformer
  [pred]
  (path/transformer
   :name "yaml-frontmatter"
   :where pred
   :metadata parse-yaml-frontmatter
   :content skip-yaml-frontmatter))

(defn yaml-frontmatter
  [& {:keys [where]}]
  (source/transform-paths (yaml-frontmatter-transformer where)))
