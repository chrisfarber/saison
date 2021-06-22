(ns saison.transform.yaml-frontmatter
  (:require [saison.source :as source]
            [saison.path :as path]
            [saison.frontmatter :as fm]
            [clj-yaml.core :as yaml]
            [saison.content :as content]
            [saison.util :as util]))

(defn parse-yaml-frontmatter
  [path]
  (with-open [stream (content/input-stream path)]
    (let [orig-meta (path/metadata path)
          meta-str (fm/frontmatter stream)]
      (if meta-str
        (util/deep-merge orig-meta
                         (yaml/parse-string meta-str))
        orig-meta))))

(defn skip-yaml-frontmatter
  [path]
  (with-open [stream (content/input-stream path)]
    (fm/skip-frontmatter stream)))

(defn yaml-frontmatter-transformer
  [pred]
  (path/transformer
   :name "yaml-frontmatter"
   :where pred
   :metadata parse-yaml-frontmatter
   :content skip-yaml-frontmatter))

(defn yaml-frontmatter
  "Parse YAML frontmatter from path content. The frontmatter will be
   merged into the path's metadata and removed from its content.
   You must supply a `:where` predicate that decides which paths will
   be scanned for frontmatter."
  [& {:keys [where]}]
  (source/transform-paths (yaml-frontmatter-transformer where)))
