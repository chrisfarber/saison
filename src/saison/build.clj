(ns saison.build
  "Tools for statically outputting a site."
  (:require [clojure.java.io :as io]
            [saison.content :as content]
            [saison.path :as path]
            [saison.proto :as proto]
            [saison.source :as source]))

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

(defn build-site
  ([site] (build-site site nil))

  ([site {:keys [verbose? publish?]
          :or {verbose? false
               publish? false}}]
   (binding [source/*publishing* publish?]
     (let [dest-path (:output-to site)
           {:keys [env constructor]} site
           source (constructor env)]
       (proto/start source env)
       (proto/before-build-hook source env)
       (when publish?
         (proto/before-publish-hook source env))
       (let [dest-file (io/file dest-path)
             all-paths (proto/scan source)]
         (doseq [p all-paths]
           (let [output-path (str "." (path/pathname p))
                 output-file (io/file dest-file output-path)]
             (when verbose? (println "Writing file:" (-> output-file
                                                         .getCanonicalFile
                                                         .getPath)))
             (write-path p output-file))))
       (proto/stop source env)))))
