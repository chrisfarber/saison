(ns saison.transform.inject-script
  (:require [net.cgrand.enlive-html :as html]
            [saison.content.html :refer [edit-html]]
            [saison.path :as path]
            [saison.source :as source]))

(defn content-script-injector [script-text]
  (fn [path]
    (edit-html (path/content path)
               [:body]
               (html/append {:tag "script"
                             :attrs {"type" "module"}
                             :content script-text}))))

(defn script-injector
  "A path transformer that will add the supplied script text
   to the end of the body tag for any path given to it."
  [script-text]
  (path/transformer
   {:content (content-script-injector script-text)}))

(defn inject-script
  "A source step that will inject the supplied script text
   into any html path."
  [script-text]
  (source/map-paths-where path/html?
                          (script-injector script-text)))