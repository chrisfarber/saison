(ns saison.source
  (:require [clojure.spec.alpha :as s]
            [saison.callable]))

(s/def :saison.source/type :saison.callable/ref)

(s/def :saison.core/source
  (s/keys :req-un [:saison.source/type]))
