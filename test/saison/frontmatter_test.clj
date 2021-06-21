(ns saison.frontmatter-test
  (:require [saison.frontmatter :as sut]
            [clojure.test :as t :refer [deftest is]]
            [saison.content :as content]
            [clojure.string :as str]))

(def ex-simple
  "

---
hello: true
---

goodbye

")

(def ex-no-frontmatter
  "there

---
is some content
---

but no frontmatter")

(deftest frontmatter-returns-nil-when-no-frontmatter
  (is (nil? (sut/frontmatter (content/input-stream ex-no-frontmatter)))))

(deftest frontmatter-gets-lines-between-marker
  (is (= "hello: true\n"
         (sut/frontmatter (content/input-stream ex-simple)))))

(deftest skip-frontmatter-with-no-frontmatter
  (println "huh" (sut/skip-frontmatter
                  (content/input-stream ex-no-frontmatter)))
  (is (= ex-no-frontmatter (str/trim
                            (sut/skip-frontmatter
                             (content/input-stream ex-no-frontmatter))))))

(deftest skip-matter-with-frontmatter
  (is (= "\ngoodbye\n\n" (sut/skip-frontmatter
                          (content/input-stream ex-simple)))))
