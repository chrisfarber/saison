(ns saison.transform.html-template
  "transform paths by templating them with enlive"
  (:require [clojure.string :as str]
            [saison.content.html :as htmlc]
            [saison.path :as path]
            [saison.source :as source]
            [net.cgrand.enlive-html :as html]))

(path/deftransform template-path-using
  [template-fn]

  (content [content metadata]
           (let [html-content (htmlc/content->html content)]
             (str/join (template-fn html-content metadata)))))

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
    (template-path-using template-fn))))
