(ns saison.live
  "Tools for running a live preview of a saison site.

  Primarily, this provides some ring middleware and server."
  (:require [clojure.core.async :as a :refer [<! <!! >! go-loop put!]]
            [clojure.java.io :as io]
            [clojure.tools.logging :as log]
            [ring.adapter.jetty :refer [run-jetty]]
            [ring.middleware.stacktrace :refer [wrap-stacktrace]]
            [saison.content :as content]
            [saison.nic :as nic]
            [saison.path :as path]
            [saison.proto :as proto]
            [saison.source :as source]
            [saison.transform.inject-script :refer [inject-script]]
            [saison.util :as util])
  (:import java.net.URL))

(def ^{:private true}
  reload-script (slurp (io/resource "saison/reloader.js")))

(defn- site-handler
  "Creates a ring handler that renders any discoverable path."

  [paths-atom]
  (fn [req]
    (let [path (:uri req)
          paths @paths-atom
          match (or (path/find-by-path paths path)
                    (path/find-by-path paths (util/add-path-component path "index.html")))]
      (if (some? match)
        (let [metadata (path/metadata match)
              mime (or (:mime-type metadata)
                       "text/plain")]
          {:status 200
           :body (content/input-stream match)
           ;; specify the mime type based on the matching path; this allows index.html to work.
           :headers {"Content-Type" mime}})
        {:status 404
         :headers {"Content-Type" "text/html"}
         :body "not found."}))))

(defn- wait-for-change [changes-atom respond]
  (let [key (gensym "wait-for-change-")]
    (log/trace "browser waiting for change" key)
    (add-watch changes-atom key (fn [_ _ old new]
                                  (when (not= old new)
                                    (log/trace "notifying browser of change" key)
                                    (try
                                      (respond {:status 204})
                                      ;; There's no way to know ahead of time if the connection
                                      ;; has since been closed; here we just ignore any exception
                                      ;; so that it doesn't interrupt the paths atom.
                                      (catch Exception _))
                                    (remove-watch changes-atom key))))))

(defn- use-site-handler [handler req respond raise]
  (try
    (respond (handler req))
    (catch Exception e
      (raise e))))

(defn- build-reloadable-source
  "Given a site definition, construct the site with previewing
   enabled (`source/*previewing*) and automatically inject the
   reload script into all javascript files."
  [{:keys [env constructor]}]
  (binding [source/*previewing* true]
    (source/construct (constructor env)
                      (inject-script :script-text reload-script))))

(defn- reloading-site-handler
  [paths-atom]
  (let [handler (wrap-stacktrace (site-handler paths-atom))]
    (fn [req respond raise]
      (let [path (:uri req)]
        (if (= path "/__reload")
          (wait-for-change paths-atom respond)
          (use-site-handler handler req respond raise))))))

(defn watch-source [source env]
  (proto/start source env)
  (proto/before-build-hook source env)
  (let [paths (atom (proto/scan source))
        change-chan (a/chan (a/sliding-buffer 1))
        update-chan (a/chan (a/sliding-buffer 1))
        stop-watcher (proto/watch source (fn [] (put! change-chan true)))
        stop (fn []
               (stop-watcher)
               (proto/stop source env)
               (a/close! change-chan)
               (a/close! update-chan))]
    (go-loop []
      (log/trace "watch-source is waiting for a watcher")
      (when (<! change-chan)
        (log/trace "watch-source watcher fired. setting timer.")
        (loop []
          (a/alt! change-chan (do
                                (log/trace "watch-source received another update, restarting timer.")
                                (recur))
                  (a/timeout 500) nil))
        (log/trace "watch-source is notifying of an update")
        (when (>! update-chan true)
          (recur))))
    (future
      (loop []
        (when (<!! update-chan)
          (log/trace "watch-source is updating the paths atom")
          (try
            (reset! paths (proto/scan source))
            (log/trace "watch-source has updated the paths atom")
            (catch Throwable e
              (log/error e "failed to scan the source")))
          (recur))))
    [paths stop]))

(defn configure-env
  "Derive a new site map with the environment ready for live previewing.
  Will auto-configure the public-url to be ready for local-network browsing if none
  is supplied. A custom :port may also be provided."
  [site {:keys [public-url port]
         :or {port 1931}}]
  (let [intended-url (or public-url
                         (if port
                           (str (URL. "http" (or (nic/guess-local-ip) "localhost") port "/"))
                           (str (URL. "http" (or (nic/guess-local-ip) "localhost")))))]
    (assoc-in site [:env :public-url] intended-url)))

(configure-env {} {})

(defn preview!
  "Start a jetty server that renders a live preview of the provided saison site.

  A map of jetty parameters may optionally be supplied. By default, the server
  will run on port 1931, the year Orval was founded.

  The returned value will be a map of data. Call `stop!` on this to shutdown the
  server"

  ([site] (preview! site {}))
  ([site & {:keys [jetty-opts
                   public-url]}]
   (let [jetty-opts (merge {:port 1931
                            :join? false
                            :async? true}
                           jetty-opts)
         site (configure-env site {:public-url public-url
                                   :port (:port jetty-opts)})
         source (build-reloadable-source site)
         env (:env site)
         [paths-atom stop] (watch-source source env)
         handler (reloading-site-handler paths-atom)]
     (log/info "starting server with public-url:" (:public-url env))
     (let [jetty (run-jetty handler jetty-opts)]
       {:paths paths-atom
        :site site
        :stop-watching stop
        :jetty jetty}))))

(defn stop!
  [inst]
  (let [{:keys [stop-watching jetty]} inst]
    (stop-watching)
    (.stop jetty)))
