(ns saison.content
  (:require [clojure.java.io :as io]
            [saison.proto :as proto]))

(defmulti input-stream
  "Coerce content to an input stream.

  Should be used with with-open to ensure that the stream is closed."
  type)

(defmulti string
  "Coerce content to a string."
  type)

(derive java.io.File ::streamable)
(derive java.io.InputStream ::streamable)

(defmethod input-stream ::streamable
  [value]
  (io/input-stream value))

(defmethod input-stream String
  [string]
  (-> string
      .getBytes
      java.io.ByteArrayInputStream.))

(defmethod input-stream saison.proto.Path
  [path]
  (input-stream (proto/content path)))

(defmethod string ::streamable
  [streamable]
  (with-open [stream (io/input-stream streamable)]
    (slurp stream)))

(defmethod string String
  [s]
  s)

(defmethod string saison.proto.Path
  [path]
  (string (proto/content path)))
