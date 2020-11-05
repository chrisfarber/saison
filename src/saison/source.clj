(ns saison.source
  (:require [saison.proto :as proto]
            [saison.util :as util]
            [saison.path :as path]))

(defmulti parse-source-form first)

(defmethod parse-source-form 'input
  [source-form]
  (let [[_ source] source-form]
    (list [:scan `(fn [paths#] (concat paths# (proto/scan ~source)))]
          [:watch `(fn [cb#] (proto/watch ~source cb#))])))

(defmethod parse-source-form 'inputs
  [[_ & sources]]
  (let [source-syms (flatten sources)
        cb (gensym "watch-cb")
        scan-forms (map (fn [sym]
                          `(map proto/scan ~sym))
                        source-syms)
        watch-forms (map (fn [sym]
                           `(map #(proto/watch % ~cb) ~sym))
                         source-syms)]
    (list [:scan `(fn [paths#]
                    (flatten (concat paths#
                                     ~@scan-forms)))]
          [:watch `(fn [~cb]
                     (let [watchers# (flatten (list ~@watch-forms))]
                         (doall watchers#)
                         (fn []
                           (doseq [watcher# watchers#]
                             (watcher#)))))])))

(defmethod parse-source-form 'filter
  [source-form]
  (let [[vec-or-sym & forms] (rest source-form)]
    (cond
      (symbol? vec-or-sym)
      (list [:scan `(fn [paths#] (filter ~vec-or-sym paths#))])

      (vector? vec-or-sym)
      (if (= 1 (count vec-or-sym))
        (list [:scan `(fn ~vec-or-sym ~@forms)])
        (throw (ex-info "a filter form must have exactly one binding", {:form source-form})))
      :else (throw (ex-info "filter's first argument must be a vector or symbol" {:form source-form})))))

(defmethod parse-source-form 'map
  [[_ fn]]
  (list [:scan `(fn [paths#] (map ~fn paths#))]))

(defmethod parse-source-form 'map-where
  [[_ pred map-fn]]
  (list [:scan `(fn [paths#]
                  (map #(if (~pred %)
                          (~map-fn %)
                          %)
                       paths#))]))

(defmethod parse-source-form 'watch
  [[_ bindings & fn-forms]]
  {:pre [(vector? bindings)
         (= 1 (count bindings))]}
  (list [:watch `(fn ~bindings ~@fn-forms)]))

(defmethod parse-source-form 'transform
  [[_ bindings & forms]]
  {:pre [(vector? bindings)
         (= 1 (count bindings))]}
  (list [:scan `(fn ~bindings ~@forms)]))

(defn- apply-step [hooks source-form]
  (reduce (fn [m [hook-name hook-fn]]
            (update-in m [hook-name] (fn [fns]
                                       (if (nil? fns)
                                         [hook-fn]
                                         (conj fns hook-fn)))))
          hooks
          (parse-source-form source-form)))

(defn- scan-using [scanners]
  (reduce (fn [paths step]
            (step paths))
          ()
          scanners))

(defn- watch-using [watchers cb]
  (let [close-fns (doall (map #(% cb) watchers))]
    (fn []
      (doseq [close-fn close-fns]
        (close-fn)))))

(defn- notify-hooks [hook-fns env]
  (doseq [h-fn hook-fns]
    (h-fn env)))

(defmacro construct
  {:style/indent [0 :defn [1]]}
  [& source-forms]
  (let [hooks (reduce apply-step {} source-forms)]
    `(reify proto/Source
       (scan [this]
         (~scan-using ~(:scan hooks)))

       (watch [this cb#]
         (~watch-using ~(:watch hooks) cb#))

       (before-build-hook [this env#]
         (~notify-hooks ~(:before-build hooks) env#))

       (before-publish-hook [this env#]
         (~notify-hooks ~(:before-publish hooks) env#)))))

(defmacro defsource
  "Define a source. "
  {:style/indent [2 :defn]}
  [source-sym bindings & source-forms]

  `(defn ~source-sym ~bindings
     (construct ~@source-forms)))

(defn concat-sources
  [& sources]
  (construct
   (inputs sources)))

(comment
  (defsource simple [source]
    (input source)
    (transform [paths]
               (rest paths))))
