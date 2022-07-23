(ns saison.transform.yaml-frontmatter-test
  (:require [saison.transform.yaml-frontmatter :as sut]
            [clojure.test :as t :refer [deftest is]]
            [saison.source :as source]
            [saison.source.data :as data]
            [saison.proto :as proto]
            [saison.path :as path]
            [saison.content :as content]))

(deftest yaml-frontmatter-reads-metadata
  (let [src (source/construct
             (data/source {:pathname "/index.md"
                           :metadata {:extra true
                                      :html-meta {:a 1
                                                  :b 1}}
                           :content "
---
from-yaml: true
html-meta:
  b: 2
---

this is more content
"})
             (sut/yaml-frontmatter :where (constantly true)))
        paths (proto/scan src)
        path (first paths)
        metadata (path/metadata path)
        content (content/string path)]
    (is (= {:extra true
            :html-meta {:a 1
                        :b 2}
            :from-yaml true}
           metadata))
    (is (= "\nthis is more content\n" content))))

(deftest yaml-frontmatter-is-not-present
  (let [content-str "a line that is not frontmatter
---
from-yaml: true
html-meta:
  b: 2
---

this is more content
"
        src (source/construct
             (data/source {:pathname "/index.md"
                           :metadata {:extra true}
                           :content content-str})
             (sut/yaml-frontmatter :where (constantly true)))
        paths (proto/scan src)
        path (first paths)
        metadata (path/metadata path)
        content (content/string path)]
    (is (= {:extra true}
           metadata))
    (is (= content-str content))))

(deftest empty-content-is-not-null
  (let [content-str "
---
hello: true
---
"
        src (source/construct
             (data/source {:pathname "/index.md"
                           :content content-str})
             (sut/yaml-frontmatter :where (constantly true)))
        paths (proto/scan src)
        path (first paths)
        content (content/string path)]
    (is (not (nil? content)))
    (is (string? content))))