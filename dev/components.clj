(ns components
  {:clj-kondo/config '{:lint-as {saison.component/defcomponent clojure.core/defn}}}
  (:require [saison.component :as c :refer [defcomponent]]))

(defcomponent :path-metadata
  []
  (let [meta (c/path-metadata)]
    [:div
     [:h3 "This Path's Metadata"]
     [:dl
      (for [[k v] meta]
        (list [:dt nil k]
              [:dd nil v]))]]))

(defcomponent :component-with-content
  []
  (let [content (c/content)]
    [:div {:wrapped true
           :style "border: 1px solid green"}
     content]))

(defcomponent :path-list
  []
  [:div
   [:p "This path is: " (c/path)]
   [:p "all paths:"]
   [:ul
    (for [[pathname meta] (c/paths)]
      (let [title (or (:title meta) pathname)]
        [:li [:a {:href pathname} title]]))]])
