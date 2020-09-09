(ns saison.source
  (:require [saison.proto :as proto]
            [saison.util :as util]))

(defn transform-source
  "derive a new source path invokes paths-modfier on the scanned
  paths"
  [source paths-modifier]
  (reify
    proto/Source
    (scan [this]
      (paths-modifier (proto/scan source)))
    (watch [this changed]
      (proto/watch source changed))))

(defn concat-sources
  [& sources]
  (reify
    proto/Source
    (scan [this]
      (mapcat proto/scan sources))
    (watch [this changed]
      (let [close-fns (map #(proto/watch % changed) sources)]
        (fn []
          (doseq [close-fn close-fns]
            (close-fn)))))))

(defn map-paths
  [source map-path]
  (transform-source source #(map map-path %)))

(defn filter-source
  [source filter-path]
  (transform-source source #(filter filter-path %)))

(defn filter-source-by-file-ext
  [source ext]
  (filter-source source
                 #(= ext (-> %
                             proto/url-path
                             util/path-extension))))

;; what should be done about 
(defn map-source-by-file-ext
  "accepts a source and a map of file extensions to source transformers.
  any path not matching a supplied file extension will be filtered out."
  [source source-ext-map]
  (apply concat-sources
         (map (fn [[ext subsource]]
                (subsource (filter-source-by-file-ext source ext)))
              source-ext-map)))
