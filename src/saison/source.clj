(ns saison.source
  (:require [saison.proto :as proto]))

(def ^:dynamic *previewing*
  "true when the the site needs to be live-reloadable"
  false)

(def ^:dynamic *publishing*
  "true when the site is being published (and should be minified, have timestamps updated, etc)"
  false)

(defn from
  "A step that consumes the provided `sources`."
  [& sources]
  (let [sources (flatten sources)]
    (reduce (fn [hooks src]
              (into hooks
                    [[:scan (fn [paths] (into paths (proto/scan src)))]
                     [:watch (fn [cb] (proto/watch src cb))]
                     [:start (fn [env] (proto/start src env))]
                     [:stop (fn [env] (proto/stop src env))]
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

(defn start
  "A function to be invoked when the source is started. Will receive a single map,
   containing the keys `:source` and `:env`"
  [f]
  [[:start f]])

(defn stop
  "A function to be invoked when the source is stopped. Will receive a single map,
   containing the keys `:source` and `:env`"
  [f]
  [[:stop f]])

(defn before-build
  "A function to be invoked before building the source. Will receive a single map,
   containing the keys `:source` and `:env`"
  [f]
  [[:before-build f]])

(defn before-publish
  "A function to be invoked before publishing the source. Will receive a single map,
   containing the keys `:source` and `:env`"
  [f]
  [[:before-publish f]])

(defn transform-paths
  "Apply a transformation to each path that flows through the source.
   
   By default, the transformed paths are cached. This can be disabled via the
   :cache option."
  [transformer & {:keys [cache] :or {cache true}}]
  (if-not cache
    [[:scan #(mapv transformer %)]]
    (let [xf-cache (atom {})]
      [[:scan
        (fn [paths]
          (let [previous @xf-cache
                next (reduce (fn [c path]
                               (assoc c path (if-let [derived (previous path)]
                                               derived
                                               (transformer path))))
                             {}
                             paths)]
            (reset! xf-cache next)
            (into [] (vals next))))]
       [:stop (fn [_] (reset! xf-cache {}))]])))

(defn transform-paths-contextually
  "Similar to `transform-paths`, but where the transform is dependent upon
   the entire set of paths, rather than a specific individual path.
   
   Use sparingly, as this defeats various optimizations and caching strategies.
   
   `transform-builder` is expected to return a transformer and will be called
   with the vector of paths that have been emitted so far."
  [transform-builder]
  [[:scan (fn [paths] (mapv (transform-builder paths) paths))]])

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

(defn when-previewing
  "Run the provided steps only when the site is constructed for live-reloading."
  [& step-vecs]
  (if *previewing*
    (apply steps step-vecs)
    []))

(defn when-publishing
  "Run the provided steps only when the site is constructed for publishing."
  [& step-vecs]
  (if *publishing*
    (apply steps step-vecs)
    []))

(defn construct [& step-vecs]
  (let [hooks (apply steps step-vecs)
        grouped-hooks (group-by first hooks)
        get-hooks (fn [n] (map second (-> grouped-hooks n (or []))))
        scanners (get-hooks :scan)
        watchers (get-hooks :watch)
        start-fns (get-hooks :start)
        stop-fns (get-hooks :stop)
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

      (start [this env]
        (notify-hooks start-fns {:source this
                                 :env env}))

      (stop [this env]
        (notify-hooks stop-fns {:source this
                                :env env}))

      (before-build-hook [this env]
        (notify-hooks before-build-fns {:source this
                                        :env env}))

      (before-publish-hook [this env]
        (notify-hooks before-publish-fns {:source this
                                          :env env})))))
