(ns saison.proto)

(defprotocol Source
  (scan [this]
    "return a list of Paths")
  (watch [this changed]
    "ask the source to invoke `changed` whenever its `scan` function
    might return different paths")

  (start [this env]
    "Called after the source is constructed.")
  (stop [this env]
    "Called to shut down the source after the site has been bult.")

  (before-build-hook [this env]
    "Called before a build is created. Also called when live preview detects new
changes.")

  (before-publish-hook [this env]
    "Called before a publish is triggered."))

(defprotocol Path
  (pathname [this]
    "compute the URL name of the path")
  (metadata [this]
    "retrieve the metadata for this path")
  (content [this]
    "compiles the path, returning its content.

    the returned content must be compatible with
     `saison.content/input-stream`."))
