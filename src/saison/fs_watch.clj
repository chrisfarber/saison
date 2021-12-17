(ns saison.fs-watch
  (:import [io.methvin.watcher
            DirectoryWatcher
            DirectoryChangeEvent
            DirectoryChangeListener]
           [java.nio.file Path]))

(defn convert-event-type [^DirectoryChangeEvent evt]
  (case (str (.eventType evt))
    "MODIFY" :modify
    "CREATE" :create
    "DELETE" :delete
    "OVERFLOW" :overflow))

(defn convert-dw-event [^DirectoryChangeEvent dw-event]
  {:type (convert-event-type dw-event)
   :path (.path dw-event)
   :file (-> dw-event .path .toFile)
   :directory? (.isDirectory dw-event)
   :count (.count dw-event)})

(defn paths-from-strings [paths-seq]
  (map #(Path/of % (make-array String 0))
       paths-seq))

(defn make-listener [f]
  (reify DirectoryChangeListener
    (onEvent [this evt]
      (f (convert-dw-event evt)))))

(defn make-watcher [{:keys [paths listener]}]
  (.build
   (doto (DirectoryWatcher/builder)
     (.paths paths)
     (.listener listener))))

(defn watch!
  "Start watching `paths` for file events. `handler` should be
  a fn that will be invoked with a map of event details.
  Returns a fn that can be invoked to stop the watching.

  (the paths should be passed in as strings)"
  [& {:keys [paths
             handler]}]
  (try
    (let [paths (paths-from-strings paths)
          listener (make-listener handler)
          watcher (make-watcher {:paths paths
                                 :listener listener})]
      (.watchAsync watcher)
      watcher)
    (catch java.nio.file.NoSuchFileException e
      (throw (ex-info "Could not watch paths"
                      {:paths paths}
                      e)))))

(defn stop! [^DirectoryWatcher watcher]
  (.close watcher))
