(ns saison.live
  "Tools for running a live preview of a saison site.

  Primarily, this provides some ring middleware and server."
  (:require [ring.adapter.jetty :refer [run-jetty]]
            [ring.middleware.stacktrace :refer [wrap-stacktrace]]
            [saison.content :as content]
            [saison.path :as path]
            [saison.proto :as proto]
            [saison.source :as source]
            [saison.transform.inject-script :refer [inject-script]]
            [saison.util :as util]
            [clojure.java.io :as io]))

(def ^{:private true}
  reload-script (slurp (io/resource "saison/reloader.js")))

(defn- site-handler
  "Creates a ring handler that renders any discoverable path."

  [site source]
  (fn [req]
    (let [env (:env site)
          path (:uri req)
          paths (proto/scan source)
          match (or (path/find-by-path paths path)
                    (path/find-by-path paths (util/add-path-component path "index.html")))]
      (if (some? match)
        (let [pathname (path/pathname match)
              metadata (path/metadata match paths env)
              mime (or (:mime-type metadata)
                       "text/plain")]
          (println "serving:" pathname)
          (println "metadata:" metadata)
          {:status 200
           :body (content/input-stream (path/content match paths env))
           ;; specify the mime type based on the matching path; this allows index.html to work.
           :headers {"Content-Type" mime}})
        {:status 404
         :headers {"Content-Type" "text/html"}
         :body "not found."}))))

(defn- wait-for-change [changes-atom respond]
  (let [key (gensym "wait-for-change-")]
    (println "waiting for change")
    (add-watch changes-atom key (fn [_ _ old new]
                                  (when (not= old new)
                                    (println "change occurred")
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
  [site]
  (let [source (build-reloadable-source site)
        changes (atom 0)
        env (:env site)
        handler (wrap-stacktrace (site-handler site source))]
    (proto/start source env)
    (proto/before-build-hook source env)
    (proto/watch source (fn []
                          (proto/before-build-hook source env)
                          (swap! changes inc)))
    (fn [req respond raise]
      (let [path (:uri req)]
        (if (= path "/__reload")
          (wait-for-change changes respond)
          (use-site-handler handler req respond raise))))))

(defn live-preview
  "Start a jetty server that renders a live preview of the provided saison site.

  A map of jetty parameters may optionally be supplied. By default, the server
  will run on port 1931, the year Orval was founded.

  Returns the jetty instance, which you can later (.stop on)"

  ([site] (live-preview site {}))
  ([site jetty-opts]
   (let [jetty-opts (merge {:port 1931
                            :join? false
                            :async? true}
                           jetty-opts)
         handler (reloading-site-handler site)]
     (run-jetty handler jetty-opts))))
