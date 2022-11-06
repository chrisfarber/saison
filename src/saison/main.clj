(ns saison.main
  (:require
   [cli-matic.core :refer [run-cmd]]
   [saison.build :as build]
   [saison.live :as live]))

(System/setProperty "tika.config" "saison/resources/tika-config.xml")

(defn resolve-site [ns-str]
  (try
    (-> ns-str
        symbol
        requiring-resolve
        deref)
    (catch Exception e
      (throw (ex-info "Could not find site definition" {:site ns-str
                                                        :underlying-error e})))))

(defn build [{:keys [site output-to publish]}]
  (let [site (resolve-site site)]
    (build/build-site site {:verbose? true
                            :output-to output-to
                            :publish? publish})))

(defn preview [{:keys [site public-url port]}]
  (let [site (resolve-site site)]
    (live/preview! site {:public-url public-url
                         :jetty-opts {:port port
                                      :join? true}})))

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
                          :type :string}
                         {:option "publish"
                          :short "p"
                          :type :with-flag
                          :default false}]
                  :runs build}
                 {:command "preview"
                  :description "Start live-previewing the site"
                  :opts [{:option "port"
                          :short "p"
                          :as "the port to listen on (http)"
                          :type :int
                          :default 1931}
                         {:option "public-url"
                          :short "u"
                          :as "the public-url the site will be built with"
                          :type :string}]
                  :runs preview}]})

(defn -main [& args]
  (run-cmd args configuration))
