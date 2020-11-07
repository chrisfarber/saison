(ns saison.proto)

(defprotocol Source
  (scan [this]
    "return a list of Paths")
  (watch [this changed]
    "ask the source to invoke `changed` whenever its `scan` function
    might return different paths")

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

    it should be compatible with `saison.content/input-stream`.

    `saison.path/*paths*` is expected to be bound to a list of all known paths.
    `saison.path/*env*` is expected to be bound to any relevant site environment.

    These bindings enable the path to dynamically compute content or
    references to other paths. it is probably not good practice for a path
    to generate another path, at the risk of causing an infinite loop."))

