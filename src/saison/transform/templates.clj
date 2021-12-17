(ns saison.transform.templates
  "transform paths by templating them with enlive"
  (:require [saison.fs-watch :as fsw]
            [net.cgrand.enlive-html :as html]
            [saison.content.html :as htmlc :refer [edit* edits]]
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
        html-meta (:html-meta metadata)]
    (edits
     [:head]
     (html/append (map (fn [[prop value]]
                         {:tag :meta
                          :attrs {:name (name prop) :content value}})
                       html-meta)))))

(defn- insert-content
  [content-selector]
  (fn [path]
    (let [html-content (htmlc/html path)]
      (edits
       [content-selector] (html/substitute html-content)))))

(defn apply-template [templates]
  (path/transformer
   :name "template"
   ;; TODO - cache the templates when possible
   :cache false
   :where (fn [path]
            (let [m (path/metadata path)
                  template (:template m)]
              (and template
                   (path/html? path)
                   (get templates template))))
   :content (fn [path]
              (let [metadata (path/metadata path)
                    template (get templates (:template metadata))
                    {:keys [file content-selector]} template
                    apply-template ((insert-content content-selector) path)
                    edit-builders (:edits template)
                    apply-edits (cond (sequential? edit-builders) (map #(% path) edit-builders)
                                      (fn? edit-builders) (edit-builders path)
                                      :else identity)]
                (edit*
                 (slurp file)
                 apply-template
                 apply-edits)))))

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
        (let [template-watcher (fsw/watch! :paths files-to-watch
                                           :handler (fn [_] (cb)))]
          #(fsw/stop! template-watcher)))))))
