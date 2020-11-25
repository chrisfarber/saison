(ns saison.site
  (:require [clojure.spec.alpha :as s]
            [saison.path :as path]
            [saison.proto :as proto]))

(s/def ::name string?)
(s/def ::output string?)
(s/def ::source fn?)

(s/def ::site (s/keys :req-un [::source
                               ::name
                               ::output]))
