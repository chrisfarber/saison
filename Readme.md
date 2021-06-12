# saison: a static content pipeline

saison is a toolkit for building static web sites with clojure.

It's alpha-quality, and there may be breaking API changes.

![Clojure CI](https://github.com/chrisfarber/saison/workflows/Clojure%20CI/badge.svg)

## Why?

I thought it would be interesting to have a static site generator
where the central abstraction is applying transformations to an HTML
AST, rather than smashing strings together.

For example, this allows you to do some interesting things:

- Automatically include a link tag for `highlight.js` in any path that
  contains at least one `<pre><code>...` block
- Programatically rewrite all your HTML headings on all your pages so
  that they have
  [screen reader friendly](https://amberwilson.co.uk/blog/are-your-anchor-links-accessible/)
  anchor tags
- Statically render pseudo-components inside of pages

Having a simple hobby project to distract me from the pandemic and
politics of 2020 was also a driving motivator.

## Features

Currently, saison supports:

- Live-reloading local preview
- Markdown processing
- Editing and templating HTML as an AST
- Atom feed generation
- Creation and publishing timestamps
- Resolving path aliases in `href` attrs to full paths

I plan for saison to support these features:

- Easily build ClojureScript source
- Dead-link checking
- Other build-time linting and analysis

## Concepts

Other static site generation tools provide a templating language for
inserting and wrangling content into your HTML.

Saison, by contrast, has no templating language. Instead, you can
manipulate an AST of your HTML using Clojure code. The AST and tools
for manipulating it are provided by Christophe Grand's excellent
[enlive](https://github.com/cgrand/enlive).

Before we can get into any of that, however, there are a few primary
concepts that we must cover.

### Content

The content of a path is simply the data that will be written to file
on disk when the site is built.

Naturally, the type of content will vary. Some paths will contain HTML
content, while others may be binary data like images. Furthur, how we
programatically represent the data during transformation may vary. It
could be a parsed AST, a simple string, a pointer to a file on disk,
or even a remote URL whose data should be fetched.

For this reason, saison has a polymorphic system for handling
content.

This system is defined in the `saison.content` namespace, but is
pretty simple. Essentially, any content that can be represented in
saison must have, at minimum, implementations for the two following
multimethods that are dispatched on `type`:

- `content/string`
- `content/input-stream`

Beyond this, saison also has a `saison.content.html` namespace for
manipulating HTML data using enlive's AST.

Before using any content from a path, or during a transformation, you
must choose how you'd like to use the content and call the appropriate
function to coerce the content.

Consider the following example:

```clj
(saison.content.html/html some-content)
```

Whether `some-content` is a string containing HTML or a
`java.io.File`, its content will be read and a parsed enlive AST will
be returned. If `some-content` happens to _already_ be a parsed AST,
then this call will behave as the `identity` function.

#### HTML Manipulation

Although saison uses enlive's data structures for representing HTML,
there's one critical detail:

Given that enlive represents HTML with regular Clojure data, we must
tag the content with metadata so that it can be dispatched on
`type`. The `htmlc/as-html` function does this, by using `vary-meta`
to tag its `:type` as `:saison.content.html/html`.

As a convenience, this namespace also exposes a `select` function that
calls enlive's select and tags it as html for you.

Otherwise, you're encouraged to use enlive directly.

Finally, this namespace has some tools for applying transformations:
the `edit-html` macro and its related functions.

Consider the following example:

```clj
(ns example
  (:require [net.cgrand.enlive-html :as html]
            [saison.content.html :as htmlc]
            [saison.content :as content]))

(content/string
  (htmlc/edit-html "<p>this is a <span>normal</span> paragraph</p>"
    [:span] (html/content "special")))

;; => "<p>this is a <span>special</span> paragraph</p>"
```

### Paths

Paths are individual pages of your site. They're composed of three
things:

- the pathname, such as "/about.html"
- metadata
- content

The metadata of a page usually includes some useful attributes like
the page title, its mime type, and a short name to refer to it.

Ultimately, all of the metadata is thrown away when writing the path
to disk. Its primary purpose is to be used by transformations applied
at the Source and Path levels.

All paths conform to the `saison.proto/Path` protocol, but the
recommended way to access any data from a path is to use the
`pathname`, `metadata`, and `content` functions from the `saison.path`
namespace.

The `saison.path` namespace includes some tools for creating
transformations to paths. These transformations can change anything
about the path, and are created using the `transformer` function.

#### Defining Transforms

Saison provides a function, `transformer`, for defining path
transformations.

Let's consider an example:

```clj
(defn change-title [new-title]
  (path/transformer
   {:metadata (fn [original-path]
                (assoc (path/metadata original-path) :title new-title))}))
```

This snippet will define a function, `change-title`. The function returned
by `change-title` will operate on individual paths, and will change the
input paths' metadata to have the `:title` of `new-title`.

##### Transforming metadata and content

For examples of transforming the content or metadata, check out the
[inject script](https://github.com/chrisfarber/saison/blob/main/src/saison/transform/inject_script.clj)
and
[markdown](https://github.com/chrisfarber/saison/blob/main/src/saison/transform/markdown.clj)
builtins.

### Sources

A Source is a provider of Paths. It is also responsible for watching
content for changes, in order to facilitate live previewing. Finally,
a Source can optionally respond to some lifecycle hooks: currently:

- `start`
- `stop`
- `before-build`
- `before-publish`

Saison includes two types of root sources out of the box:

- files, which provides paths from the filesystem
- data, which provides paths from regular clojure data

But, a source may be anything that implements the
`saison.proto/Source` protocol.

Sources are meant to be composed together; i.e., to form a source
tree. In addition to simply concatenating sources together, it's
common to filter or apply transformations to paths originating in
other sources.

To facilitate this, the `saison.source` namespace provides some tools:

```clojure
(source/construct
  (files {:root "blog"})
  (files {:root "css"}))
```

or, to filter out all paths that aren't html:

```clojure
(source/construct
  other-source
  (source/filter-paths path/html?))
```

Most often, you'll want to apply a transformation:

```clj
(source/construct
  other-source
  (source/transform-paths (my-transformation 42)))
```

### Site Definition

Finally, the site definition is just a clojure map that wraps up a few
things:

- `:constructor`, a function that creates the site's source
- `:env`, common configuration or data available to sources and paths
- `:output-to`, the folder where files will be written during build

The environment map can have anything you find useful in it. It will
be bound to the var `saison.path/*env*` when invoking the `metadata`
or `content` functions on any path.

The `:env` should contain the key `:public-url`, which will be used to
generate absolute URLs.

#### Example site

```clj
(ns example.site
  (:require [saison.source.file :refer [files]]
            [saison.transform.markdown :refer [markdown]]
            [saison.transform.edn-metadata :refer [file-metadata]]
            [saison.transform.short-name-links :refer [short-name-links]]
            [saison.transform.html-template :as templ :refer [templates]]
            [saison.source :as source]
            [saison.live :as live]))

(defn constructor [env]
  (source/construct
   (files {:root "pages"
           :metadata {:template "page"}})
   (file-metadata)
   (markdown)
   (templates
    {:file "templates/page.html"
     :name "page"
     :edits [templ/set-title
             templ/apply-html-metadata]})
   (aliases/resolve-path-aliases))

(def site
  {:output-to "dist"
   :env {:public-url "https://my-project.github.io"}
   :constructor  constructor)})

(defonce live-preview-server (atom nil))

(defn stop! []
  (when-let [server @live-preview-server]
    (.stop server)
    (reset! live-preview-server nil)))

(defn start! []
  (stop!)
  (let [local-site (assoc-in site [:env :public-url] "http://localhost:1931")
        server (live/live-preview local-site {:port 1931})]
    (reset! live-preview-server server)))

(comment
  (start!)
  )
```

#### Reloading the site

During preview, saison will live-reload your site in the browser
whenever underlying filesystem content changes. This isn't true out of
the box for the site's source construction.

The above example has `start!` and `stop!` functions to facilitate code
reloading in a repl workflow.

If using cider, you may want to add these to .dir-locals.el:

```elisp
((nil . ((cider-ns-refresh-before-fn . "docs/stop!")
	 (cider-ns-refresh-after-fn . "docs/start!")
	 )))
```

## Running

Saison provides a simple command line tool capable of building your
site and running a live-reloading preview server.

To build, use the following:

```sh
clj -m saison.main -s my.site/config build
```

Where `my.site/config` is the qualified keyword pointing to your site
definition.

To run a live preview, use:

```sh
clj -m saison.main -s my.site/config preview
```

By default, saison will then listen on `http://localhost:1931`.
