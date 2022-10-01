(ns saison.proto)

(defprotocol Path
  (pathname [this]
    "compute the URL name of the path")
  (metadata [this]
    "retrieve the metadata for this path")
  (content [this]
    "compiles the path, returning its content.

    the returned content must be compatible with
     `saison.content/input-stream`."))
