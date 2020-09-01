(ns saison.proto)

(defprotocol Source
  (scan [this])
  (watch [this changed]))

(defprotocol Path
  (url-path [this]
    "retrieve the path component of the URL")
  (metadata [this]
    "retrieve a metadata map for this path")
  (generate [this paths site]
    "compiles the path
    a string, or an input stream, is returned.

    the `paths` argument should be a list of all other paths identified
    for the site. this enables the path to dynamically compute content or
    references to other paths. it is probably not good practice for a path
    to generate another path, at the risk of causing an infinite loop."))
