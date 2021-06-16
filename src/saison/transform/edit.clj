(ns saison.transform.edit
  (:require [saison.content.html :as htmlc]
            [saison.path :as path]
            [saison.source :as source]))

(defn apply-edits [pred edits]
  (path/transformer
   :name "edit-path"
   :where pred
   :content (fn [path]
              (let [content (path/content path)]
                (htmlc/edit* content edits)))))

(defn pred-for [pathname-or-pred]
  (cond (string? pathname-or-pred)
        (fn [path] (= pathname-or-pred
                      (path/pathname path)))

        (fn? pathname-or-pred)
        pathname-or-pred))

(defn edit-path*
  [path-name-or-pred & edit-fns]
  (source/transform-paths
   (apply-edits (pred-for path-name-or-pred)
                edit-fns)))

(defmacro edit-path
  [path-name-or-pred & rules]
  `(edit-path* ~path-name-or-pred
               (htmlc/edits ~@rules)))
