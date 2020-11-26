(ns saison.transform.edit-page
  (:require [saison.source :as source]
            [saison.path :as path]
            [saison.content.html :as htmlc]
            [net.cgrand.enlive-html :as html]))

(path/deftransform edit-path
    [build-edit-ops]
  (content [path content]
    (htmlc/edit-html* content (build-edit-ops path))))

(defn pred-for [pathname-or-pred]
  (cond (string? pathname-or-pred) (fn [path] (= pathname-or-pred
                                                 (path/pathname path)))
        (fn? pathname-or-pred) pathname-or-pred))

(defn edit-page*
  {:style/indent [2 :form]}
  [source pred build-edit-ops]
  (source/construct
    (input source)
    (map-where pred (edit-path build-edit-ops))))

(defmacro edit-page
  {:style/indent [2 :defn]}
  [source path-name-or-pred path-binding & forms]
  {:pre [(vector? path-binding)
         (= 1 (count path-binding))]}
  `(edit-page*
       ~source
       (pred-for ~path-name-or-pred)
     (fn ~path-binding
       ~@forms)))
