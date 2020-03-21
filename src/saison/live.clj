(ns saison.live
  "Tools for running a live preview of a saison site.

  Primarily, this provides some ring middleware and server."
  (:require [saison.core :as sn]
            [ring.adapter.jetty :refer [run-jetty]]
            [ring.middleware.content-type :refer [wrap-content-type]]))

(defn path-matches?
  "Does the second path match the first?

  This is slightly more complex than an equality check, as we have to
  deal with some issues like matching index.html and trailing slashes."

  [target actual]
  (let [actual actual])
  (cond
    (= target actual) 1
    ))

(defn site-handler
  "Creates a ring handler that renders any discoverable path."

  [site]
  (fn [req]
    (let [path (:uri req)
          paths (sn/discover-paths site)
          matches (filter #(= path (:path %)) paths)]
      (println "uri:" path)
      (println "matches:" (count matches))
      (println "paths:" paths)
      (if (not-empty matches)
        {:status 200
         :body (sn/compile-path site paths (first matches))}
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
