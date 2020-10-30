(ns saison.transform.inject-script
  (:require [net.cgrand.enlive-html :as html]
            [saison.content.html :refer [edit-html]]
            [saison.path :as path]
            [saison.source :as source]))

(path/deftransform path-inject-script
  [script-text]

  (content [content]
           (edit-html content
                      [:body]
                      (html/append {:tag "script"
                                    :attrs {"type" "module"}
                                    :content script-text}))))

(defn inject-script
  "for html paths, inject a script module at the end of the body"
  ([script-text]
   #(inject-script % script-text))

  ([source script-text]
   (source/map-paths-where source path/is-html? (path-inject-script script-text))))
