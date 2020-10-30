(ns saison.transform.html-template
  "transform paths by templating them with enlive"
  (:require [clojure.java.io :as io]
            [net.cgrand.enlive-html :as html]
            [saison.content.html :as htmlc :refer [alter-html-content edits edit-html]]
            [saison.path :as path]
            [saison.proto :as proto]
            [saison.source :as source]
            [hawk.core :as hawk]))

(defn set-title
  [path]
  (let [metadata (path/path->metadata path)
        title (:title metadata)]
    (edits
     [:head :title] (html/content title))))

(defn apply-html-metadata
  [path]
  (let [metadata (path/path->metadata path)
        html-meta (:html-meta-tags metadata)]
    (edits
     [:head [:meta html/first-of-type]]
     (html/clone-for [[prop value] html-meta]
                     [:meta] (html/set-attr :name prop
                                            :content value)))))

(defn- insert-content
  [content-selector]
  (fn [path]
    (let [content (path/path->content path)
          html-content (htmlc/content->html content)]
      (edits
       [content-selector] (html/substitute html-content)))))

(path/deftransform apply-template
  [templates]

  (content [path metadata content]
           (let [template (get templates (:template metadata))
                 {:keys [file content-selector]} template
                 apply-template ((insert-content content-selector) path)
                 edit-builders (:edits template)
                 apply-edits (cond (sequential? edit-builders) (map #(% path) edit-builders)
                                   (fn? edit-builders) (edit-builders path)
                                   :else identity)]
             (edit-html
              (slurp file)
              apply-template
              apply-edits))))

(defn templates
  "Transform a source by applying templates to paths.

  A path must opt-in to having a template applied to it. This is done by
  setting the `:template` metadata to a string that identifies the desired
  template.

  When constructing this source, any number of template definitions can be
  supplied after the origin source. Each template definition should be a
  map with the following keys:

  `:file` - something that can be `slurp`ed to get an html string
  `:name` - a string identifier of the template
  `:content-selector` - an enlive selector indicating where to insert the
                        content. optional.
  `:edits` - a function, or list of functions, that accepts a path and returns
             transformations defined via the `edits` macro. optional."
  [source & template-defs]
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
        files-to-watch (map :file template-defs)
        find-template (fn [path]
                        (let [m (path/path->metadata path)
                              template (:template m)]
                          (and template
                               (get templates template))))
        templating-source (source/map-paths-where source find-template (apply-template templates))]
    (reify
      proto/Source

      (scan [this]
        (proto/scan templating-source))
      (watch [this cb]
        (let [close-source (proto/watch templating-source cb)
              template-watcher (hawk/watch! [{:paths files-to-watch
                                              :handler (fn [_ _]
                                                         (cb))}])]
          (fn []
            (hawk/stop! template-watcher)
            (close-source)))))))

