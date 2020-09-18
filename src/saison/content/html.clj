(ns saison.content.html
  (:require [clojure.string :as str]
            [net.cgrand.enlive-html :as html]
            [saison.content :as content]))

(defmethod content/content->string ::html
  [html-seq]
  (str/join (html/emit* html-seq)))

(defmethod content/content->input-stream ::html
  [html-seq]
  (-> html-seq
      content/content->string
      content/content->input-stream))

(defn as-html [html-seq]
  (vary-meta html-seq assoc :type ::html))

(defmulti content->html
  "Get the content as a parsed enlive nodes"
  type)

(defmethod content->html ::content/streamable
  [content]
  (as-html
   (with-open [stream (content/content->input-stream content)]
     (html/html-resource stream))))

(defmethod content->html java.lang.String
  [content]
  (as-html
   (html/html-snippet content)))

(defmethod content->html ::html
  [content]
  content)

(comment
  (alter-html [content]
              (html/at )))

(defmacro alter-html-content
  "Parse content as html using enlive, and call `as-html` on the
  result.

  There must be exactly one binding. `content->html` will be called
  on its value."
  [binding & body]
  (assert (vector? binding) "binding should be a vector")
  (assert (= 2 (count binding)) "binding should have two symbols")
  (let [[name source] binding]
    `(as-html
      (let [~name (content->html ~source)]
        ~@body))))
