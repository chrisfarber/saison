(ns saison.transform.edit
  (:require [saison.source :as source]
            [saison.path :as path]
            [saison.content.html :as htmlc]
            [net.cgrand.enlive-html :as html]))

(defn apply-edits [build-edit-ops]
  (path/transformer
   {:content (fn [path]
               (let [content (path/content path)]
                 (htmlc/edit-html* content (build-edit-ops path))))}))

(defn pred-for [pathname-or-pred]
  (cond (string? pathname-or-pred) (fn [path] (= pathname-or-pred
                                                 (path/pathname path)))
        (fn? pathname-or-pred) pathname-or-pred))

(defn edit-path*
  [pred build-edit-ops]
  (source/map-paths-where pred (apply-edits build-edit-ops)))

(defmacro edit-path
  {:style/indent [2 :defn]}
  [path-name-or-pred path-binding & forms]
  {:pre [(vector? path-binding)
         (= 1 (count path-binding))]}
  `(edit-path*
    (pred-for ~path-name-or-pred)
    (fn ~path-binding
      ~@forms)))
