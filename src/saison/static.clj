(ns saison.static
  (:require [saison.util :as util]))

(declare static-path)

(defn source
  "A source for static, unprocessed files."
  [source-config]
  (let [{:keys [path]} source-config
        files (util/list-files path)]
    (map static-path
         files)))

(defn generate
  [_ _ path]
  (-> path
      :data
      :file))

(defn- static-path
  [[name f]]
  {:full-path name
   :generator 'saison.static/generate
   :data {:file f}})
