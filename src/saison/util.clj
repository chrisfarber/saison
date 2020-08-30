(ns saison.util
  (:require [clojure.java.io :as io]
            [clojure.string :as str]))

(defn list-files
  "Return a seq of files in a directory.

  Each item is a vec of [relative full-path], where
  the first item is the path relative to the provided folder
  and the second item is the canonical path"
  [path]

  (let [root (io/file path)]
    (filter
     (fn [[_ f]] (.isFile f))
     (tree-seq
      (fn [[^String _ ^java.io.File f]] (.isDirectory f))
      (fn [[^String relPath ^java.io.File d]]
        (map (fn [^java.io.File f]
               [(str relPath java.io.File/separator (.getName f)) f])
             (.listFiles d)))
      ["" root]))))

(defn add-path-component
  "Add a path component to a string. Handles trailing slashes on the base."
  [base addition]
  (let [base (or base "")
        base (if-not (str/starts-with? base "/")
               (str "/" base)
               base)
        addition (or addition "")]
    (str base
         (when-not (str/ends-with? base "/")
           "/")
         (second (re-find #"^/*([^/]?.*[^/]+)/*$" addition)))))

(defn to-input-stream [file-path-or-stream]
  (cond
    (= (type file-path-or-stream) java.lang.String) (recur (io/file file-path-or-stream))
    (= (type file-path-or-stream) java.io.File) (io/input-stream file-path-or-stream)
    (= (type file-path-or-stream) java.io.InputStream) file-path-or-stream))
