(ns saison.transform.inject-script
  (:require  [saison.source :as source]
             [saison.path :as path]
             [saison.proto :as proto]
             [saison.content.html :refer [alter-html-content]]
             [net.cgrand.enlive-html :as html]))

(defn- path-inject-script
  [path script-text]
  (path/derive-path
   path
   {:content (fn [path paths env]
               (let [oc (proto/content path paths env)]
                 (alter-html-content [html oc]
                                     (html/at html
                                              [:body] 
                                              (html/append {:tag "script"
                                                            :attrs {"type" "module"}
                                                            :content script-text})))))}))

(defn inject-script
  "for html paths, inject a script module at the end of the body"
  ([script-text]
   #(inject-script % script-text))
  
  ([source script-text]
   (source/map-paths-where source path/is-html? #(path-inject-script % script-text))))
