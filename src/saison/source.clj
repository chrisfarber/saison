(ns saison.source
  (:require [saison.proto :as proto]))

(defn from
  "A step that consumes the provided `sources`."
  [& sources]
  (let [sources (flatten sources)]
    (reduce (fn [hooks src]
              (into hooks
                    [[:scan (fn [paths] (into paths (proto/scan src)))]
                     [:watch (fn [cb] (proto/watch src cb))]
                     [:before-build (fn [env] (proto/before-build-hook src env))]
                     [:before-publish (fn [env] (proto/before-publish-hook src env))]]))
            []
            sources)))

(defn emit
  "A step that adds the provided `paths` to the source's output."
  [& paths]
  [[:scan (fn [other-paths] (into other-paths (flatten paths)))]])

(defn add-watcher
  [watcher]
  [[:watch watcher]])

(defn before-build [fn]
  [[:before-build fn]])

(defn before-publish [fn]
  [[:before-publish fn]])

(defn map-paths
  "Invoke `map-fn` on each path that flows through it."
  [map-fn]
  [[:scan (fn [paths] (mapv map-fn paths))]])

(defn map-paths-where
  "Invoke `map-fn` on each path that `predicate` returns a truthy value for."
  [predicate map-fn]
  (map-paths #(if (predicate %)
                (map-fn %)
                %)))

(defn filter-paths
  [predicate]
  [[:scan (fn [paths] (filterv predicate paths))]])

(defn modify-paths
  "A source step that receives the vector of all paths and can return a new path vector."
  [fn]
  [[:scan fn]])

(defn- notify-hooks [fns & args]
  (doseq [fn fns]
    (apply fn args)))

(defn steps
  "Combine multiple step vectors into one.

  If a source is provided, it will be wrapped automatically using `from`.
  If nil is provided, it is ignored."
  [& step-vecs]
  (transduce (comp (filter some?)
                   (map #(if (satisfies? saison.proto/Source %)
                           (from %)
                           %)))
             (completing into)
             []
             step-vecs))

(defn construct [& hook-lists]
  (let [hooks (apply steps hook-lists)
        grouped-hooks (group-by first hooks)
        get-hooks (fn [n] (map second (-> grouped-hooks n (or []))))
        scanners (get-hooks :scan)
        watchers (get-hooks :watch)
        before-build-fns (get-hooks :before-build)
        before-publish-fns (get-hooks :before-publish)]
    (reify proto/Source
      (scan [_]
        (reduce (fn [paths step]
                  (step paths))
                []
                scanners))

      (watch [_ cb]
        (let [close-fns (doall (map #(% cb) watchers))]
          (fn stop-watching []
            (doseq [close-fn close-fns]
              (close-fn)))))

      (before-build-hook [this env]
        (notify-hooks before-build-fns {:source this
                                        :env env}))

      (before-publish-hook [this env]
        (notify-hooks before-publish-fns {:source this
                                          :env env})))))
