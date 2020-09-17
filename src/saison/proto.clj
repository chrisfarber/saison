(ns saison.proto)

(defprotocol Source
  (scan [this]
    "return a list of Paths")
  (watch [this changed]
    "ask the source to invoke `changed` whenever its `scan` function
    might return different paths"))

(defprotocol Path
  (path [this]
    "compute the URL name of the path")
  (metadata [this]
    "retrieve the metadata for this path")
  (content [this paths site]
    "compiles the path
    a string, or an input stream, is returned.

    the `paths` argument should be a list of all other paths identified
    for the site. this enables the path to dynamically compute content or
    references to other paths. it is probably not good practice for a path
    to generate another path, at the risk of causing an infinite loop."))
