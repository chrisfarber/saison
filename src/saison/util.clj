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
  (str base
       (when-not (str/ends-with? base "/")
         "/")
       (second (re-find #"^/*([^/]?.*[^/]+)/*$" addition))))

(defn invoke
  "Similar to apply, but will use `requiring-resolve` on qualified symbols."
  [fn-or-sym & args]
  (let [fn (if (qualified-symbol? fn-or-sym)
             (requiring-resolve fn-or-sym)
             fn-or-sym)]
    (apply fn args)))
