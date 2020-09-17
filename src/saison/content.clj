(ns saison.content
  (:require [clojure.java.io :as io]))

(defmulti content->input-stream
  "Coerce data to an input stream.

  Should be used with with-open to ensure that the stream is closed."
  type)

(derive java.io.File ::streamable)
(derive java.io.InputStream ::streamable)

(defmethod content->input-stream ::streamable
  [value]
  (io/input-stream value))

(defmethod content->input-stream java.lang.String
  [string]
  (-> string
      .getBytes
      java.io.ByteArrayInputStream.))

(comment
  (with-open [data (content->input-stream "it works?")]
    (slurp data))
  )

