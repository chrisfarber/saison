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

(defn path-extension
  [url-path]
  ;; stolen from ring:
  (if-let [ext (second (re-find #"\.([^./\\]+)$" url-path))]
    (str/lower-case ext)))

(defn compound-path-extension
  [url-path]
  (if-let [ext (second (re-find #"\.([^/\\]+)$" url-path))]
    (str/lower-case ext)))

(defn set-path-extension [url-path new-ext]
  (str/replace url-path #"\.[^./\\]+$"
               (str "." new-ext)))

(defmulti ->input-stream
  "Convert the item to an input stream."
  type)

(defmethod ->input-stream java.lang.String
  [path]
  (->input-stream (io/file path)))

(defmethod ->input-stream java.io.File
  [f]
  (io/input-stream f))

(defmethod ->input-stream java.io.InputStream
  [stream]
  stream)

(defn data->input-stream
  "take some string data and make an input stream out of it"
  [data]
  (-> data
      .getBytes
      java.io.ByteArrayInputStream.))

(defn input-stream->string
  [is]
  (slurp is))
