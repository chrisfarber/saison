(ns saison.main
  (:require [cli-matic.core :refer [run-cmd]]
            [clojure.pprint :as pprint]))

(def cli-options
  [["-s" "--site SITE" "A qualified, resolvable symbol of the site definition"
    :parse-fn #(symbol %)]
   ["-b" "--build" "build the site"]])

(defn build [args]
  (println "got?" args))

(def configuration
  {:command "saison"
   :description "static site generation"
   :subcommands [{:command "build"
                  :description "build a site"
                  :opts [{:option "site"
                          :short "s"
                          :type :string}]
                  :runs build}]})

(defn -main [& args]
  (run-cmd args configuration))
