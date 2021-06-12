(ns saison.transform.templates
  "transform paths by templating them with enlive"
  (:require [hawk.core :as hawk]
            [net.cgrand.enlive-html :as html]
            [saison.content.html :as htmlc :refer [edit-html* edits]]
            [saison.source :as source]
            [saison.path :as path]))

(defn set-title
  [path]
  (let [metadata (path/metadata path)
        title (:title metadata)]
    (edits
     [:head :title] (html/content title)
     [:h1#title] (html/content title))))

(defn apply-html-metadata
  [path]
  (let [metadata (path/metadata path)
        html-meta (:html-meta-tags metadata)]
    (edits
     [:head [:meta html/first-of-type]]
     (html/clone-for [[prop value] html-meta]
                     [:meta] (html/set-attr :name prop
                                            :content value)))))

(defn- insert-content
  [content-selector]
  (fn [path]
    (let [content (path/content path)
          html-content (htmlc/html content)]
      (edits
       [content-selector] (html/substitute html-content)))))

(defn apply-template [templates]
  ;; TODO when transformer caching is implemented, make sure we invalidate it
  ;;      when templates change.
  (path/transformer
   (fn [path]
     (let [m (path/metadata path)
           template (:template m)]
       (and template
            (path/html? path)
            (get templates template))))
   {:content (fn [path]
               (let [metadata (path/metadata path)
                     template (get templates (:template metadata))
                     {:keys [file content-selector]} template
                     apply-template ((insert-content content-selector) path)
                     edit-builders (:edits template)
                     apply-edits (cond (sequential? edit-builders) (map #(% path) edit-builders)
                                       (fn? edit-builders) (edit-builders path)
                                       :else identity)]
                 (edit-html*
                  (slurp file)
                  apply-template
                  apply-edits)))}))

(defn templates
  "A source step that applies templates to paths.

  A path must opt-in to having a template applied to it. This is done by
  setting the `:template` metadata to a string that identifies the desired
  template.

  Each template definition should be a
  map with the following keys:

  `:file` - something that can be `slurp`ed to get an html string
  `:name` - a string identifier of the template
  `:content-selector` - an enlive selector indicating where to insert the
                        content. optional.
  `:edits` - a function, or list of functions, that accepts a path and returns
             transformations defined via the `edits` macro. optional."
  [& template-defs]
  (let [templates (reduce (fn [ts def]
                            (let [{:keys [file name edits content-selector]
                                   :or {content-selector :div#content}} def
                                  template {:file file
                                            :content-selector content-selector
                                            :edits edits}]
                              (assoc ts name
                                     template)))
                          {}
                          template-defs)
        files-to-watch (map :file template-defs)]
    (source/steps
     (source/transform-paths (apply-template templates))
     (source/add-watcher
      (fn [cb]
        (let [template-watcher (hawk/watch! [{:paths files-to-watch
                                              :handler (fn [_ _] (cb))}])]
          #(hawk/stop! template-watcher)))))))
