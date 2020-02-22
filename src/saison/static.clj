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
           {:saison.path/path name
            :saison.path/generator 'saison.static/generator
            :saison.path/data {:file f}})
         files)))


