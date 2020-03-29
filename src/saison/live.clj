(ns saison.live
  "Tools for running a live preview of a saison site.

  Primarily, this provides some ring middleware and server."
  (:require [saison.core :as sn]
            [ring.adapter.jetty :refer [run-jetty]]
            [ring.middleware.content-type :refer [wrap-content-type]]
            [saison.util :as util]
            [ring.util.mime-type :as mime]))

(defn site-handler
  "Creates a ring handler that renders any discoverable path."

  [site]
  (fn [req]
    (let [path (:uri req)
          paths (sn/discover-paths site)
          matches (filter #(or (= path (:path %))
                               (= (util/add-path-component path "index.html") (:path %))) paths)
          match (first matches)]
      (if (some? match)
        {:status 200
         :body (sn/compile-path site paths match)
         ;; specify the mime type based on the matching path; this allows index.html to work.
         :headers {"Content-Type" (mime/ext-mime-type (:path match))}}
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
  (.stop testing-site)
  )
