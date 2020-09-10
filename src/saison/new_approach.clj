(ns saison.new-approach
  (:require [saison.source.file :refer [files]]
            [saison.build :as build]))

(comment

  (def simple-site
    {:output-to "./dist1"
     :sources [(files {:root "./fixtures"})]})
  (build/build-site simple-site)

  
  "organization...
- saison.path
- saison.source
- saison.source.file
- saison.source.data
- saison.source.cljs
- saison.transform.html-template
- saison.transform.markdown
- saison.transform.rss
- saison.build
- saison.main
- saison.live


source -> mapper -> mapper -> filter -> [paths]

source -> splitter -> [paths]
           -> mapper
           -> filter -> mapper


"

  ;; sketching out some options below...
  (def site-with-ioc
    {:output-to "dist"
     :sources [(cljs {:build-config cfg
                      :output "/js/stuff.js"
                      :alias "stuff.js"})
               (-> {:scan "fixtures/markdown1"
                    :base-path "blog"
                    :meta {:blog true}}
                   files
                   markdown
                   hoist-attrs)
               (hoist-attrs (markdown (files {:scan "fixtures/markdown1"
                                              :base-path "blog"
                                              :meta {:blog true}})))
               (map-suffixes (files {:scan "somewhere"})
                             {".md" #(markdown % {:option1 :something})
                              ".html" [basic-template "whatever"]})
               (blog-index {:base-path "blog"
                            :output "blog/index.html"
                            :short-name "blog-home"})
               (rss-feed {:base-path "blog"
                          :output "blog/feed.xml"
                          :short-name "blog-index"})
               (files {:from "something/about.html"
                       :output "/about.html"
                       :short-name "about"})
               (files {:scan "pages"
                       :output "/"})]})

  (def markdown-2
    {:Sources [(Wrap-With-Template
                {:File "Templates/Meh"}
                (Markdown {:Scan "Fixtures/Markdown1"
                           :Base-Path "blog"}))
               (blog-index {:base-path "blog"
                            :output "blog/index.html"})
               (rss-feed {:base-path "blog"
                          :output "blog/feed.xml"})
               (static {:file "something/about.html"
                        :output "about.html"})
               (static {:scan "pages"
                        :output "/"})]}))
