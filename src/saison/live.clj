(ns saison.live
  "Tools for running a live preview of a saison site.

  Primarily, this provides some ring middleware and server."
  (:require [saison.site :as sn]
            [ring.adapter.jetty :refer [run-jetty]]
            [ring.middleware.content-type :refer [wrap-content-type]]
            [saison.util :as util]
            [ring.util.mime-type :as mime]
            [saison.path :as path]
            [saison.proto :as proto]))

(defn site-handler
  "Creates a ring handler that renders any discoverable path."

  [site]
  (fn [req]
    (let [path (:uri req)
          paths (sn/discover-paths site)
          match (or (path/find-by-path paths path)
                    (path/find-by-path paths (util/add-path-component path "index.html")))]
      (if (some? match)
        (let [pathname (proto/url-path match)
              metadata (proto/metadata match)
              mime (or (:mime-type metadata)
                       (mime/ext-mime-type pathname))]
          (println "serving:" pathname)
          (println "metadata:" metadata)
          {:status 200
           :body (sn/compile-path site paths match)
           ;; specify the mime type based on the matching path; this allows index.html to work.
           :headers {"Content-Type" mime}})
        {:status 404
         :headers {"Content-Type" "text/html"}
         :body "not found."}))))

(defn live-preview
  "Start a jetty server that renders a live preview of the provided saison site.

  A map of jetty parameters may optionally be supplied. By default, the server
  will run on port 1931, the year Orval was founded.

  Returns the jetty instance."

  ([site] (live-preview site {}))
  ([site jetty-opts]
   (let [jetty-opts (merge {:port 1931
                            :join? false}
                           jetty-opts)
         handler (wrap-content-type (site-handler site))]
     (run-jetty handler jetty-opts))))

(comment
  ;; to start
  (def testing-site
    (live-preview {:sources [{:type 'saison.static/source
                              :path "./fixtures/b"}]}))

  ;; to stop
  (.stop testing-site))
