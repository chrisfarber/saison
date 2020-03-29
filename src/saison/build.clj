(ns saison.build
  "Tools for statically outputting a site."
  (:require [saison.core :as sn]
            [saison.util :as util]
            [clojure.java.io :as io]))

(defn path-stream [path-out]
  (cond
    (= (type path-out) java.io.File) (io/input-stream path-out)
    (= (type path-out) java.io.InputStream) path-out))

(defn write-file
  "Write data to an ouput file.

  Intermediate directories will automatically be created."
  [dest contents]
  (-> dest
      io/as-file
      (.getParentFile)
      (.mkdirs))
  (with-open [out (io/output-stream dest)
              in (path-stream contents)]
    (io/copy in out)))

(defn write-path
  "Compile and write the path to the specified output.

  The path is compiled with `compile-path`. Any intermediate directories
  will be automatically created."

  [site paths path dest-file]

  (let [data (sn/compile-path site paths path)]
    (write-file dest-file data)))

(defn build-site
  [site output-path]
  (let [all-paths (sn/discover-paths site)]
    (doseq [p all-paths]
      (let [output-path (util/add-path-component output-path (:path p))]
        (write-path site all-paths p output-path)))))

(comment
  (build-site {:sources [{:type 'saison.static/source
                          :path "./fixtures/b"}]} "test-output")

  (-> "stuff/a/bc/cd.txt"
      (java.io.File.)
      (.getParentFile)
      (.mkdirs))
  )
