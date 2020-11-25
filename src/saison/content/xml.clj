(ns saison.content.xml
  (:require [clojure.data.xml :as xml]
            [saison.content :as content]))

(defmethod content/string clojure.data.xml.Element
  [xml-element]
  (xml/emit-str xml-element))

(defmethod content/input-stream clojure.data.xml.Element
  [xml-element]
  (content/input-stream
   (content/string xml-element)))
