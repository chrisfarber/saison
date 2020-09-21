(ns saison.live
  "Tools for running a live preview of a saison site.

  Primarily, this provides some ring middleware and server."
  (:require [ring.adapter.jetty :refer [run-jetty]]
            [saison.path :as path]
            [saison.proto :as proto]
            [saison.site :as sn]
            [saison.util :as util]
            [saison.content :as content]
            [saison.transform.inject-script :refer [inject-script]]))

(def ^{:private true}
  reload-script
  "
fetch(\"/__reload\").then(r => {
window.location.reload(true);
})
")

(defn- site-handler
  "Creates a ring handler that renders any discoverable path."

  [site]
  (fn [req]
    (let [path (:uri req)
          paths (sn/discover-paths site)
          match (or (path/find-by-path paths path)
                    (path/find-by-path paths (util/add-path-component path "index.html")))]
      (if (some? match)
        (let [pathname (path/path->name match)
              metadata (path/path->metadata match paths {}) ;; TODO - env
              mime (or (:mime-type metadata)
                       "text/plain")]
          (println "serving:" pathname)
          (println "metadata:" metadata)
          {:status 200
           :body (content/content->string (path/path->content match paths {})) ;; TODO - env
           ;; specify the mime type based on the matching path; this allows index.html to work.
           :headers {"Content-Type" mime}})
        {:status 404
         :headers {"Content-Type" "text/html"}
         :body "not found."}))))

(defn- wait-for-change [changes-atom respond]
  (let [key (gensym "wait-for-change-")]
    (add-watch changes-atom key (fn [_ _ old new]
                                  (when (not= old new)
                                    (respond {:status 204})
                                    (remove-watch changes-atom key))))))

(defn- use-site-handler [handler req respond raise]
  (try
    (respond (handler req))
    (catch Exception e
      (raise e))))

(defn- reloading-site-handler
  [site]
  (let [reloadable-site (update site :source (inject-script reload-script))
        site-source (:source reloadable-site)
        changes (atom 0)
        handler (site-handler reloadable-site)]
    (proto/watch site-source (fn [] (swap! changes inc)))
    (fn [req respond raise]
      (let [path (:uri req)]
        (if (= path "/__reload")
          (wait-for-change changes respond)
          (use-site-handler handler req respond raise))))))

(defn live-preview
  "Start a jetty server that renders a live preview of the provided saison site.

  A map of jetty parameters may optionally be supplied. By default, the server
  will run on port 1931, the year Orval was founded.

  Returns the jetty instance."

  ([site] (live-preview site {}))
  ([site jetty-opts]
   (let [jetty-opts (merge {:port 1931
                            :join? false
                            :async? true}
                           jetty-opts)
         handler (reloading-site-handler site)]
     (run-jetty handler jetty-opts))))

(comment
  ;; to start
  (def testing-site
    (live-preview ...site-config...))

  ;; to stop
  (.stop testing-site))
