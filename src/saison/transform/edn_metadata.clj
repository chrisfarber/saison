(ns saison.transform.edn-metadata
  (:require [clojure.edn :as edn]
            [saison.path :as path]
            [saison.proto :as proto]
            [saison.source :as source]
            [saison.util :as util]))

(defn read-meta [entry]
  ;; TODO this is an abuse of the content fn ....
  (let [output (proto/content entry (list entry) {})]
    (with-open [instream (util/->input-stream output)]
      (edn/read-string (util/input-stream->string instream)))))

(defn file-metadata
  [source]
  (source/transform-source
   source
   (fn [entries]
     (let [path-for (memoize proto/path)
           probs-meta? (fn [entry] (= "edn"
                                      (util/path-extension (path-for entry))))
           normal-entries (remove probs-meta? entries)
           meta-entries (filter probs-meta? entries)
           meta-lookup (group-by path-for meta-entries)
           update-metadata (fn [entry]
                             (let [path (path-for entry)
                                   path-meta (proto/metadata entry)
                                   meta-entry (get-in meta-lookup
                                                      [(str path ".edn") 0])]
                               (if meta-entry
                                 (merge path-meta (read-meta meta-entry))
                                 path-meta)))]
       (map #(path/derive-path % {:metadata update-metadata})
            normal-entries)))))
