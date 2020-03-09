(ns saison.static
  (:require [saison.util :as util]
            [clojure.java.io :as io]
            [clojure.spec.alpha :as s]))

(defn source
  "A source for static, unprocessed files."
  [source-config]
  (let [{:keys [path]} source-config
        files (util/list-files path)]
    (map (fn [[name f]]
           {:path name
            :generator 'saison.static/generate
            :data {:file f}})
         files)))

(defn generate
  [site-config paths path]
  (-> path
      :data
      :file))


