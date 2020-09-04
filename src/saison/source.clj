(ns saison.source
  (:require [saison.proto :as proto]
            [saison.util :as util]))

(defn derive-source
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
          (doall (map #(%) close-fns)))))))

(defn map-paths
  [source map-path]
  (derive-source source #(map map-path %)))

(defn filter-source
  [source filter-path]
  (derive-source source #(filter filter-path %)))

(defn filter-source-by-file-ext
  [source ext]
  (filter-source source
                 #(= ext (-> %
                             proto/url-path
                             util/path-extension))))

(defn map-source-by-file-ext
  "return a new source"
  [source source-ext-map]
  (apply concat-sources
         (map (fn [[ext subsource]]
                (subsource (filter-source-by-file-ext source ext)))
              source-ext-map)))

