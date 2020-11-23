(ns saison.build
  "Tools for statically outputting a site."
  (:require [clojure.java.io :as io]
            [saison.site :as sn]
            [saison.util :as util]
            [saison.path :as path]
            [saison.content :as content]
            [saison.proto :as proto]))

(defn write-file
  "Write data to an ouput file.

   Calls io/as-file on the destination, and to-input-stream on its contents
   Intermediate directories will automatically be created."
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

  [path paths env dest-file]

  (let [data (path/content path paths env)]
    (write-file dest-file data)))

(defn build-site
  ([site] (build-site site nil))
  
  ([site {:keys [verbose? publish?]
          :or {verbose? false
               publish? false}}]
   (let [dest-path (:output-to site)
         {:keys [env source]} site]
     (proto/before-build-hook source env)
     (when publish?
       (proto/before-publish-hook source env))
     (let [dest-file (io/file dest-path)
           all-paths (sn/discover-paths site)]
       (doseq [p (proto/scan source)]
         (let [output-path (str "." (path/pathname p))
               output-file (io/file dest-file output-path)]
           (if verbose? (println "Writing file:" (-> output-file
                                                     .getCanonicalFile
                                                     .getPath)))
           (write-path p all-paths env output-file)))))))
