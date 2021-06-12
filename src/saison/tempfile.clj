(ns saison.tempfile
  (:import [java.io File]))

(defmacro with-tempfile
  "Create a temporary file that will be deleted after the body is evaluated.
   The body is provided a java.io.File representing the temp file."
  [[binding name] & forms]
  `(let [temp# (File/createTempFile ~name ".temp")
         ~binding (.getPath temp#)
         r# (do ~@forms)]
     (.delete temp#)
     r#))

(defn make-temp-dir
  "Create a temporary directory with the supplied prefix.
   
   The path will be marked as `deleteOnExit`.
   Returns a java.nio.file.Path."
  [prefix]
  (let [path (java.nio.file.Files/createTempDirectory
              prefix
              (into-array java.nio.file.attribute.FileAttribute []))
        file (.toFile path)]
    (.deleteOnExit file)
    path))