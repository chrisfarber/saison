(ns saison.frontmatter
  "Functions for dealing with frontmatter streams"
  (:require [clojure.java.io :as io]
            [clojure.string :as str]))

(defn marker? [s]
  (re-find #"^---+\s*$" s))

(defn frontmatter [stream]
  (with-open [rdr (io/reader stream)]
    (let [sb (StringBuilder.)]
      (loop [reading-frontmatter false]
        (let [line (.readLine rdr)]
          (if-not reading-frontmatter
            (cond (str/blank? line) (recur false)
                  (marker? line) (recur true)
                  :else :done)
            (when-not (marker? line)
              (.append sb line)
              (.append sb "\n")
              (recur true)))))
      (when (< 0 (.length sb))
        (str sb)))))

(defn skip-frontmatter [stream]
  (with-open [rdr (io/reader stream)]
    (let [sb (StringBuilder.)
          push (fn [s]
                 (.append sb s)
                 (.append sb "\n"))]
      (loop [state nil]
        (when-let [line (.readLine rdr)]
          (cond
            (= nil state)
            (if (marker? line)
              (do
                (.setLength sb 0)
                (recur :frontmatter))
              (do (push line)
                  (recur (when-not (str/blank? line)
                           :content))))

            (= :content state)
            (do (push line)
                (recur state))

            (= :frontmatter state)
            (recur (if (marker? line)
                     :content
                     :frontmatter)))))

      (str sb))))
