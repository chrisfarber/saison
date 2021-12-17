(ns saison.content.html
  (:require [clojure.string :as str]
            [net.cgrand.enlive-html :as html]
            [saison.content :as content]
            [saison.proto :as proto]))

(defmethod content/string ::html
  [html-seq]
  (str/join (html/emit* html-seq)))

(defmethod content/input-stream ::html
  [html-seq]
  (-> html-seq
      content/string
      content/input-stream))

(defn as-html [html-seq]
  (vary-meta html-seq assoc :type ::html))

(defmulti html
  "Get the content as a parsed enlive nodes"
  type)

(defmethod html ::content/streamable
  [content]
  (as-html
   (with-open [stream (content/input-stream content)]
     (html/html-resource stream))))

(defmethod html java.lang.String
  [content]
  (as-html
   (html/html-snippet content)))

(defmethod html ::html
  [content]
  content)

(defmethod html saison.proto.Path
  [path]
  (html (proto/content path)))

(defn select [content selector]
  (as-html
   (html/select (html content) selector)))

(defmacro edits
  [& rules]
  `(fn [node-or-nodes#]
     (html/at node-or-nodes# ~@rules)))

(defn apply-edits
  [html edit-fn]
  (edit-fn html))

(defn edit*
  "Applies whatever edits are supplied to `content`.

  The content is coerced to html automatically, and the resulting
  content will be marked as html as well.

  `edit-fns` will be flattened and applied in the order they
  are specified (i.e., the opposite of `comp`)"

  [content & edit-fns]
  (as-html
   (reduce apply-edits
           (html content)
           (flatten edit-fns))))

(defmacro edit
  "Parse content as html and apply the specified enlive rules to it.

  Calls `as-html` on the result for you."
  {:style/indent [1 :form]}
  [content & rules]
  `(edit* ~content
          (edits ~@rules)))
