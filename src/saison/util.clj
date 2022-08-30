(ns saison.util
  (:require [clojure.java.io :as io]
            [clojure.string :as str]
            [tick.core :as t])
  (:import java.net.URL
           java.io.File))

(set! *warn-on-reflection* true)

(defn list-files
  "Return a seq of files in a directory.

  Each item is a vec of [relative full-path], where
  the first item is the path relative to the provided folder
  and the second item is the canonical path"
  [path]

  (let [root (io/file path)]
    (filter
     (fn [[_ ^File f]] (.isFile f))
     (tree-seq
      (fn [[^String _ ^File f]] (.isDirectory f))
      (fn [[^String relPath ^File d]]
        (map (fn [^File f]
               [(str relPath File/separator (.getName f)) f])
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
  (when-let [ext (second (re-find #"\.([^./\\]+)$" url-path))]
    (str/lower-case ext)))

(defn compound-path-extension
  [url-path]
  (when-let [ext (second (re-find #"\.([^/\\]+)$" url-path))]
    (str/lower-case ext)))

(defn set-path-extension [url-path new-ext]
  (str/replace url-path #"\.[^./\\]+$"
               (str "." new-ext)))

(defn append-url-component
  [base addition]
  (let [^URL base (if (instance? URL base)
                    base
                    (URL. base))
        base-path (.getPath base)]
    (str (URL. base (add-path-component base-path addition)))))

(defn rfc3339 [time]
  (t/format "u-MM-dd'T'HH:mm:ss.SSXXX" time))

;; https://clojure.atlassian.net/browse/CLJ-1468
(defn deep-merge
  "Like merge, but merges maps recursively."
  [& maps]
  (if (every? map? maps)
    (apply merge-with deep-merge maps)
    (last maps)))
