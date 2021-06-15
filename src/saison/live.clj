(ns saison.live
  "Tools for running a live preview of a saison site.

  Primarily, this provides some ring middleware and server."
  (:require [clojure.core.async :as a :refer [<! <!! >! go-loop put!]]
            [clojure.java.io :as io]
            [clojure.tools.logging :as log]
            [ring.adapter.jetty :refer [run-jetty]]
            [ring.middleware.stacktrace :refer [wrap-stacktrace]]
            [saison.content :as content]
            [saison.path :as path]
            [saison.proto :as proto]
            [saison.source :as source]
            [saison.transform.inject-script :refer [inject-script]]
            [saison.util :as util]))

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
        (let [pathname (path/pathname match)
              metadata (path/metadata match)
              mime (or (:mime-type metadata)
                       "text/plain")]
          (log/debug "serving" pathname)
          {:status 200
           :body (content/input-stream (path/content match))
           ;; specify the mime type based on the matching path; this allows index.html to work.
           :headers {"Content-Type" mime}})
        {:status 404
         :headers {"Content-Type" "text/html"}
         :body "not found."}))))

(defn- wait-for-change [changes-atom respond]
  (let [key (gensym "wait-for-change-")]
    (log/debug "browser waiting for change" key)
    (add-watch changes-atom key (fn [_ _ old new]
                                  (when (not= old new)
                                    (log/debug "notifying browser of change" key)
                                    (respond {:status 204})
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
                      (inject-script reload-script))))

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
          (reset! paths (proto/scan source))
          (log/trace "watch-source has updated the paths atom")
          (recur))))
    [paths stop]))

(defn live-preview
  "Start a jetty server that renders a live preview of the provided saison site.

  A map of jetty parameters may optionally be supplied. By default, the server
  will run on port 1931, the year Orval was founded.

  Returns a function that can be called to stop the server."

  ([site] (live-preview site {}))
  ([site jetty-opts]
   (let [source (build-reloadable-source site)
         env (:env site)
         jetty-opts (merge {:port 1931
                            :join? false
                            :async? true}
                           jetty-opts)
         [paths-atom stop] (watch-source source env)
         handler (reloading-site-handler paths-atom)]
     (log/info "starting server on port" (:port jetty-opts))
     (let [jetty (run-jetty handler jetty-opts)]
       (fn shutdown []
         (stop)
         (.stop jetty))))))
