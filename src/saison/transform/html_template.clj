(ns saison.transform.html-template
  "transform paths by templating them with enlive"
  (:require [clojure.string :as str]
            [saison.content.html :as htmlc]
            [saison.path :as path]
            [saison.proto :as proto]
            [saison.source :as source]))

(defn- wrap-template-fn
  [template-fn]
  (fn [path paths site]
    (let [html-content (-> path
                           (proto/content paths site)
                           htmlc/content->html)
          metadata (proto/metadata path)]
      (str/join (template-fn html-content metadata)))))

(defn- map-template
  [template-fn]
  (let [templator (wrap-template-fn template-fn)]
    (fn [original-path]
      (path/derive-path original-path
                        {:content templator}))))

(defn template-using
  "Transform a source by applying an enlive template to its paths.

  By default, the template is filtered to paths for which are marked
  as html. This can be overridden by passing in a function for
  `where?`.

  The supplied `template-fn` will receive content and metadata. The
  content will be parsed enlive nodes. The `template-fn` should return
  the same structure as an enlive template: a seq of strings to be
  concatenated."
  ([source template-fn]
   (template-using source template-fn path/is-html?))
  
  ([source template-fn where?]
   (source/map-paths-where
    source
    where?
    (map-template template-fn))))
