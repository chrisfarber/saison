(ns saison.callable
  (:require [clojure.spec.alpha :as s]))

(s/def ::ref
  (s/or :symbol qualified-symbol?
        :fn var?
        :fn fn?))

(defn invoke
  "Similar to apply, but will use `requiring-resolve` on qualified symbols."
  [fn-or-sym & args]
  (let [fn (if (qualified-symbol? fn-or-sym)
             (requiring-resolve fn-or-sym)
             fn-or-sym)]
    (apply fn args)))
