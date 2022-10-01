(ns saison.component
  {:clj-kondo/config '{:lint-as {saison.component/defcomponent clojure.core/defn}}}
  (:require [saison.source :as source]
            [saison.path :as path]
            [net.cgrand.enlive-html :as html]
            [hiccup.core :as hiccup]
            [saison.content.html :as htmlc]
            [clojure.tools.logging :as log]))

(defonce ^:private registry (atom {}))

(def ^:private normalize-tag
  (memoize #(-> % name .toLowerCase keyword)))

(defn register-component
  "Register a component handler. Tag should be a keyword, and f should be a fn of attrs -> hiccup."
  [tag f]
  (swap! registry assoc (normalize-tag tag) f))

(defn- registered-component [tag]
  (get @registry (normalize-tag tag)))

;; Defining components

(def ^:private ^:dynamic *env*
  "Components read their env (not attrs) from this dynamic var."
  nil)

(defmacro defcomponent
  [component-ident bindings & forms]
  (assert (ident? component-ident) "the component identity must be a keyword or symbol")
  (assert (vector? bindings) "must supply a binding vector, like with fn")
  (assert (<= 0 (count bindings) 2) "there must be 0-1 bindings")
  (let [id-sym (symbol component-ident)
        attrs-binding (or (first bindings) (gensym "attrs"))
        fn-form `(fn ~id-sym [~attrs-binding] ~@forms)]
    `(let [fn# ~fn-form]
       (#'register-component ~(keyword component-ident) fn#)
       (def ~id-sym fn#))))

(defn enlive->hiccup
  "semi-lazily convert enlive's data structure into a hiccup data structure"
  [form]
  (cond (map? form) (let [{:keys [tag attrs content]} form]
                      [tag attrs (enlive->hiccup content)])
        (or (vector? form) (seq? form)) (map enlive->hiccup form)
        :else form))

(defn content
  "Retrieves the content passed in to the currently rendering component as hiccup."
  []
  (-> *env* :content enlive->hiccup))

(defn path
  "The pathname of the path in which the current component is being rendered into."
  []
  (-> *env* :path :pathname))

(defn path-metadata
  "The metadata of the path in which the current component is being rendered into."
  []
  (-> *env* :path :metadata))

(defn paths
  "A map of all pathnames to their metadata"
  []
  (:paths *env*))

(defn publishing?
  "Is the component being rendered while the site is publishing?"
  []
  (-> *env* :source-env :publishing))

(def previewing?
  "Is the component being rendered while the site is previewing?"
  (complement publishing?))

;; Rendering components

(defn- render-component [component-fn attrs env]
  (binding [*env* env]
    (component-fn attrs)))

(defn- registry-selectors
  "Prepare an enlive selector that will recognize the registry's components"
  []
  [(html/pred (fn [n] (contains? @registry (normalize-tag (:tag n)))))])

(defn- render-component-for-node [node env]
  (if-let [component (registered-component (:tag node))]
    ;; this is a hilariously inefficient way to convert hiccup -> enlive...
    ;; I'm hoping it won't matter as subtrees will be shallow
    (html/html-snippet
     (hiccup/html (render-component component
                                    (:attrs node)
                                    (merge env
                                           {:content (:content node)}))))
    {:tag :div
     :content ["No component registered " (pr-str (:tag node))]}))

(defn- render-components-in-path
  [path env]
  (htmlc/edit path
              (registry-selectors)
              #(render-component-for-node % env)))

(defn- render-components-in-paths
  [path-vec]
  ;; unclear whether caching the path-info map will cause problems.
  (let [path-info-map (path/path-info path-vec)
        source-env source/*env*]
    (path/transformer
     :cache false
     :where path/html?
     :content #(render-components-in-path % {:path {:pathname (path/pathname %)
                                                    :metadata (path/metadata %)}
                                             :paths path-info-map
                                             :source-env source-env}))))

(defn- watch-registry [cb]
  (let [watch-key (gensym "registry-watcher")]
    (log/debug "watching component registry with key" watch-key)
    (add-watch registry watch-key
               (fn [_ _ _ _]
                 (log/debug "component registry changed, notifying watcher")
                 (cb)))
    (fn []
      (log/debug "removing registry watcher")
      (remove-watch registry watch-key))))

(defn render-components
  "A source step that will render components in HTML paths, and automatically reload
   whenever the component registry changes."
  []
  (source/steps
   (source/transform-paths-contextually #'render-components-in-paths)
   (source/on-watch #'watch-registry)))
