(ns saison.content
  (:require [clojure.java.io :as io]))

(defmulti content->input-stream
  "Coerce content to an input stream.

  Should be used with with-open to ensure that the stream is closed."
  type)

(defmulti content->string
  "Soerce content to a string."
  type)

(derive java.io.File ::streamable)
(derive java.io.InputStream ::streamable)

(defmethod content->input-stream ::streamable
  [value]
  (io/input-stream value))

(defmethod content->input-stream String
  [string]
  (-> string
      .getBytes
      java.io.ByteArrayInputStream.))

(defmethod content->string ::streamable
  [streamable]
  (with-open [stream (io/input-stream streamable)]
    (slurp stream)))

(defmethod content->string String
  [s]
  s)
