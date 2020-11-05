(ns saison.build
  "Tools for statically outputting a site."
  (:require [clojure.java.io :as io]
            [saison.site :as sn]
            [saison.util :as util]
            [saison.path :as path]
            [saison.content :as content]))

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
              in (content/content->input-stream contents)]
    (io/copy in out)))

(defn write-path
  "Compile and write the path to the specified output.

  The path is compiled with `compile-path`. Any intermediate directories
  will be automatically created."

  [site paths path dest-file]

  (let [data (path/content site paths path)]
    (write-file dest-file data)))

(defn build-site
  ([site] (build-site site false))
  
  ([site verbose?]
   (let [dest-path (:output-to site)
         dest-file (io/file dest-path)
         all-paths (sn/discover-paths site)]
     (doseq [p all-paths]
       (let [output-path (str "." (path/pathname p))
             output-file (io/file dest-file output-path)]
         (if verbose? (println "Writing file:" (-> output-file
                                                   .getCanonicalFile
                                                   .getPath)))
         (write-path site all-paths p output-file))))))

