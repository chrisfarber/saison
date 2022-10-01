(ns saison.build
  "Tools for statically outputting a site."
  (:require [clojure.java.io :as io]
            [saison.content :as content]
            [saison.path :as path]
            [saison.source :as source])
  (:import [java.util.concurrent ConcurrentLinkedQueue]))

(defn write-file
  "Write data to an ouput file.

   Calls `io/as-file` on the destination, and `content/input-stream` on its
   contents. Intermediate directories will automatically be created."
  [dest contents]
  (-> dest
      io/as-file
      (.getParentFile)
      (.mkdirs))
  (with-open [out (io/output-stream dest)
              in (content/input-stream contents)]
    (io/copy in out)))

(defn write-path
  "Compile and write the path to the specified output.

  Any intermediate directories will be automatically created."

  [path dest-file]

  (let [data (path/content path)]
    (write-file dest-file data)))

(defn process-queue [queue dest-file]
  (loop []
    (when-let [path (.poll queue)]
      (let [output-path (str "." (path/pathname path))
            output-file (io/file dest-file output-path)]
        (println "Writing file:" (-> output-file
                                     .getCanonicalFile
                                     .getPath))
        (write-path path output-file))
      (recur))))

(defn concurrent-build-paths [paths dest-file]
  (let [cpus (.availableProcessors (Runtime/getRuntime))
        queue (ConcurrentLinkedQueue. paths)
        procs (map (fn [_] (future (process-queue queue dest-file)))
                   (range cpus))]
    (dorun procs)
    (doseq [proc procs]
      @proc)))

(defn build-site
  ([source] (build-site source {:publish? false
                                :output-to "dist"}))

  ([source {:keys [publish?
                   output-to]
            :or {publish? false}}]
   (binding [source/*env* (source/environment-for source {:publishing publish?})]
     (source/start source)
     (source/before-build source)
     (when publish?
       (source/before-publish source))
     (let [dest-file (io/file output-to)
           all-paths (source/scan source)]
       (concurrent-build-paths all-paths dest-file))
     (source/stop source))))