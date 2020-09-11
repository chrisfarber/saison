(ns saison.main
  (:require [clojure.tools.cli :refer [parse-opts]]
            [clojure.pprint :as pprint]))

(def cli-options
  [["-s" "--site SITE" "A qualified, resolvable symbol of the site definition"
    :parse-fn #(symbol %)]
   ["-b" "--build" "build the site"]])

(defn -main [& args]
  (pprint/pprint (parse-opts args cli-options)))
