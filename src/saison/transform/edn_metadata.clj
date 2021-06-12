(ns saison.transform.edn-metadata
  (:require [clojure.edn :as edn]
            [saison.content :as content]
            [saison.path :as path]
            [saison.util :as util]
            [saison.source :as source]))

(defn read-meta [path]
  (let [output (path/content path)
        metadata-str (content/string output)]
    (edn/read-string metadata-str)))

(defn file-metadata
  "A source step that reads EDN metadata from paths."
  []
  ;; TODO - make this use contextualized transform
  ;; TODO - caching strategy. right now, it clobbers any hope at caching.
  (source/modify-paths
   (fn [entries]
     (let [path-for (memoize path/pathname)
           probs-meta? (fn [entry] (= "edn"
                                      (util/path-extension (path-for entry))))
           normal-entries (remove probs-meta? entries)
           meta-entries (filter probs-meta? entries)
           meta-lookup (group-by path-for meta-entries)
           update-metadata (fn [entry]
                             (let [path (path-for entry)
                                   path-meta (path/metadata entry)
                                   meta-entry (get-in meta-lookup
                                                      [(str path ".edn") 0])]
                               (if meta-entry
                                 (merge path-meta (read-meta meta-entry))
                                 path-meta)))]
       (mapv #(path/derive-path % {:metadata update-metadata})
             normal-entries)))))