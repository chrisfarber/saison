(ns saison.transform.edn-metadata
  (:require [saison.source :as source]
            [saison.proto :as proto]
            [saison.util :as util]
            [saison.path :as path]
            [clojure.edn :as edn]))

(defn read-meta [entry]
  ;; TODO this is an abuse of the generate fn ....
  (let [output (proto/generate entry (list entry) {})]
    (with-open [instream (util/->input-stream output)]
      (edn/read-string (util/input-stream->string instream)))))

(defn file-metadata
  [source]
  (source/transform-source
   source
   (fn [entries]
     (let [path-for (memoize proto/url-path)
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