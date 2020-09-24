(ns saison.transform.inject-script
  (:require  [saison.source :as source]
             [saison.path :as path]
             [saison.proto :as proto]
             [saison.content.html :refer [alter-html-content]]
             [net.cgrand.enlive-html :as html]))

(path/deftransform path-inject-script
  [script-text]

  (content [content]
           (alter-html-content [html content]
                               (html/at html
                                        [:body]
                                        (html/append {:tag "script"
                                                      :attrs {"type" "module"}
                                                      :content script-text})))))

(defn inject-script
  "for html paths, inject a script module at the end of the body"
  ([script-text]
   #(inject-script % script-text))

  ([source script-text]
   (source/map-paths-where source path/is-html? (path-inject-script script-text))))
