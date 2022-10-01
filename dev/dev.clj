(ns dev
  (:require [saison.source :as source]
            [saison.live :as live]
            [saison.source.file :refer [files]]
            [saison.transform.yaml-frontmatter :refer [yaml-frontmatter]]
            [saison.transform.markdown :refer [markdown markdown?]]
            [saison.component :refer [render-components]]
            [components]))

(def site
  (source/construct
   (files {:root "fixtures/dev"})
   (yaml-frontmatter :where markdown?)
   (markdown)
   (render-components)))

(defonce preview (atom nil))

(defn stop! []
  (when-let [inst @preview]
    (live/stop! inst)
    (reset! preview nil)))

(defn start! []
  (stop!)
  (let [inst (live/preview! site)]
    (reset! preview inst)
    nil))

(start!)