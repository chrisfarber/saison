(ns saison.source
  (:require [saison.proto :as proto]
            [saison.util :as util]
            [saison.path :as path]))

(defn transform-source
  "derive a new source path invokes paths-modfier on the scanned
  paths"
  ([paths-modifier]
   (fn [source]
     (transform-source source paths-modifier)))
  ([source paths-modifier]
   (reify
     proto/Source
     (scan [this]
       (paths-modifier (proto/scan source)))
     (watch [this changed]
       (proto/watch source changed)))))

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
  ([map-path]
   (transform-source #(map map-path %)))
  ([source map-path]
   (transform-source source #(map map-path %))))

(defn filter-source
  ([filter-path]
   (transform-source #(filter filter-path %)))
  ([source filter-path]
   (transform-source source #(filter filter-path %))))

(defn filter-source-by-file-ext
  [source ext]
  (filter-source source
                 #(= ext (-> %
                             path/path->name
                             util/path-extension))))

(defn map-paths-where
  ([pred f]
   #(map-paths-where % pred f))
  ([source pred f]
   (map-paths source (fn [path]
                       (if (pred path)
                         (f path)
                         path)))))

;; what should be done about 
(defn map-source-by-file-ext
  "accepts a source and a map of file extensions to source transformers.
  any path not matching a supplied file extension will be filtered out."
  [source source-ext-map]
  (apply concat-sources
         (map (fn [[ext subsource]]
                (subsource (filter-source-by-file-ext source ext)))
              source-ext-map)))
