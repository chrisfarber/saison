(ns saison.source
  (:import [java.util IdentityHashMap]))

(def ^:dynamic *env*)

(defn publishing? []
  (:publishing *env*))

(def previewing? (complement publishing?))

(defn emit
  "A step that emits paths from the source when it is scanned.
   
   The `paths-or-fns` can be a (nested) sequence of paths to be emitted
   or functions that produce paths to be emitted. The functions will be
   called at scan-time."
  [& paths-or-fns]
  [[:scan (fn emitter [existing-paths]
            (transduce
             (comp (map (fn [path-or-fn]
                          (if (fn? path-or-fn)
                            (path-or-fn)
                            path-or-fn)))
                   (map (fn [paths-or-path]
                          (if (sequential? paths-or-path)
                            (into [] paths-or-path)
                            [paths-or-path]))))
             (completing into)
             existing-paths
             (flatten paths-or-fns)))]])

(defn set-default-env
  "A step that will set a value in the environment _only_ if it is not
   already defined"
  [k v]
  [[:env (fn [env]
           (if (contains? env k)
             env
             (assoc env k v)))]])

(defn set-env
  "A step that sets a value in the environment during construction"
  [k v]
  [[:env #(assoc % k v)]])

(defn on-watch
  [watcher]
  [[:watch watcher]])

(defn on-start
  "A function to be invoked when the source is started."
  [f]
  [[:start f]])

(defn on-stop
  "A function to be invoked when the source is stopped."
  [f]
  [[:stop f]])

(defn before-build-hook
  "A function to be invoked before building the source."
  [f]
  [[:before-build f]])

(defn before-publish-hook
  "A function to be invoked before publishing the source."
  [f]
  [[:before-publish f]])

(defn transform-paths
  "Apply a transformation to each path that flows through the source.

   By default, the transformed paths are cached. This can be disabled via the
   :cache option."
  [transformer & {:keys [cache] :or {cache true}}]
  (if-not cache
    [[:scan #(mapv transformer %)]]
    (let [xf-cache (atom nil)]
      [[:scan
        (fn [paths]
          (let [previous @xf-cache
                next (IdentityHashMap. (count paths))]
            (doseq [path paths]
              (.put next path
                    (or (get previous path)
                        (transformer path))))
            (reset! xf-cache next)
            (into [] (vals next))))]
       [:stop (fn [_] (reset! xf-cache nil))]])))

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
  [f]
  [[:scan f]])

(defn steps
  "Combine multiple step vectors into one, ignoring nils."
  [& step-vecs]
  (transduce (filter some?)
             (completing into)
             []
             step-vecs))

(defn- wrap-step-with-predicate
  [pred step]
  (let [[step-t f] step]
    (cond
      (#{:start :stop :before-build :before-publish} step-t)
      [step-t (fn conditional-hook []
                (when (pred)
                  (f)))]

      (= :watch step-t)
      [step-t (fn conditional-watch [cb]
                (if (pred)
                  (f cb)
                  (fn [])))]

      (#{:scan :env} step-t)
      [step-t (fn conditional-thread [val]
                (if (pred)
                  (f val)
                  val))]

      :else
      (throw (ex-info
              (str "Can't make step conditional " step-t)
              {:step step
               :pred pred})))))

(defn when-previewing
  "Run the provided steps only when the site is constructed for live-reloading."
  [& step-vecs]
  (let [merged-steps (apply steps step-vecs)]
    (into [] (map #(wrap-step-with-predicate previewing? %)) merged-steps)))

(defn when-publishing
  "Run the provided steps only when the site is constructed for publishing."
  [& step-vecs]
  (let [merged-steps (apply steps step-vecs)]
    (into [] (map #(wrap-step-with-predicate publishing? %)) merged-steps)))

(defn construct
  [& step-vecs]
  (apply steps step-vecs))

(defn- step-pred
  [step-t]
  (fn step-matcher
    [step]
    (= step-t (first step))))

(defn- extract-fns-of-step
  [step-t]
  (comp (filter (step-pred step-t))
        (map second)))

(defn- thread-value-through-steps
  [source step-t initial-value]
  (transduce (extract-fns-of-step step-t)
             (completing (fn [val f] (f val)))
             initial-value
             source))

(defn- notify-hooks
  [source step-t]
  (doseq [[step f] source]
    (when (= step step-t)
      (f)))
  nil)

(defn environment-for
  "Given a source, and optionally a map containing default environment data,
   construct the actual environment."
  ([source] (environment-for source {}))
  ([source env]
   (thread-value-through-steps source :env env)))

(defn start
  "Run a source's on-start hooks.
   Ensure `*env*` is bound."
  [source]
  (notify-hooks source :start))

(defn stop
  "Run a source's on-stop hooks.
   Ensure `*env*` is bound."
  [source]
  (notify-hooks source :stop))

(defn scan
  "Find paths emitted by the source.
   Ensure `*env*` is bound."
  [source]
  (thread-value-through-steps source :scan []))

(defn watch
  "Begin watching the source for changes. `cb` should be zero-arity,
   and will be called whenever a change has been detected. May be called
   very frequently; it's up to the caller to apply any throttling logic.

   Returns a zero-arity fn that can be called to stop watching.

   Ensure `*env*` is bound."
  [source cb]
  (let [watch-fns (into [] (extract-fns-of-step :watch) source)
        stop-watchers (doall (map (fn [start-watch] (start-watch cb))
                                  watch-fns))]
    (fn stop-watching []
      (doseq [close-fn stop-watchers]
        (close-fn)))))

(defn before-build [source]
  (notify-hooks source :before-build))

(defn before-publish [source]
  (notify-hooks source :before-publish))
