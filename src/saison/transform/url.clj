(ns saison.transform.url
  (:require [saison.content.html :as htmlc]
            [saison.path :as path]
            [saison.source :as source])
  (:import [java.net URI]))

(defn absolute? [href]
  (.isAbsolute (URI. href)))

(defn update-href
  [node attr f]
  (if (contains? (:attrs node) attr)
    (update-in node [:attrs attr] f)
    node))

(defn edit-urls [f]
  (htmlc/edits
   [#{:link :a}] (fn [node] (update-href node :href f))
   [#{:img :script}] (fn [node] (update-href node :src f))))

(defn canonicalize-url
  [public-url path href]
  (if (and href (not (absolute? href)))
    (path/canonicalize
     public-url
     (path/resolve path href))
    href))

(defn canonicalize-urls-in-content [public-url]
  (fn [path]
    (htmlc/edit*
     path
     (edit-urls #(canonicalize-url public-url path %)))))

(defn canonicalize-path
  [public-url]
  (path/transformer
   :name "canonicalize-urls"
   :where path/html?
   :content (canonicalize-urls-in-content public-url)))

(defn canonicalize-urls
  "Canonicalize hrefs. Any non-absolute URI in any HTML page
   will be converted into an absolute URI relative to the supplied
   :public-url."
  [& {:keys [public-url]}]
  (source/transform-paths (canonicalize-path public-url)))
