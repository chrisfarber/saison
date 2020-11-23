(ns saison.main
  (:require [cli-matic.core :refer [run-cmd]]
            [saison.build :as build]
            [saison.live :as live]))

(defn resolve-site [ns-str]
  (try
    (-> ns-str
        symbol
        requiring-resolve
        deref)
    (catch Exception e
      (throw (ex-info "Could not find site definition" {:site ns-str
                                                        :underlying-error e})))))

(defn build [{:keys [site output-to]}]
  (let [site (resolve-site site)
        site (if output-to
               (assoc site :output-to output-to)
               site)]
    (build/build-site site {:verbose? true})))

(defn preview [{:keys [site port]}]
  (let [site (resolve-site site)]
    (live/live-preview site {:port port
                             :join? true})))

(def configuration
  {:command "saison"
   :version "0.0.1"
   :description "static site generation"
   :opts [{:option "site"
           :short "s"
           :type :string
           :as "A resolvable symbol of the site configuration"
           :default :present}]
   :subcommands [{:command "build"
                  :description "Generate the site and write it out"
                  :opts [{:option "output-to"
                          :short "o"
                          :type :string}]
                  :runs build}
                 {:command "preview"
                  :description "Start live-previewing the site"
                  :opts [{:option "port"
                          :short "p"
                          :as "the port to listen on (http)"
                          :type :int
                          :default 1931}]
                  :runs preview}]})

(defn -main [& args]
  (run-cmd args configuration))
