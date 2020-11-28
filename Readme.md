# saison: a static content pipeline

saison is a toolkit for building static web sites with clojure.

It's alpha-quality, and there may be breaking API changes.

## Why?

I thought it would be interesting to have a static site generator
where the central abstraction is applying transformations to an HTML
AST, rather than smashing strings together.

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

## Documentation

TODO.

## Concepts

Other static site generation tools provide a templating language for
inserting and wrangling content into your HTML.

Saison, by contrast, has no templating language. Instead, you can
manipulate an AST of your HTML using Clojure code. The AST and tools
for manipulating it are provided by Christophe Grand's excellent
[enlive](https://github.com/cgrand/enlive).

Before we can get into any of that, however, there are a few primary
concepts that we must cover.

### Sources

A Source is a provider of Paths. It is also responsible for watching
content for changes, in order to facilitate live previewing. Finally,
a Source can optionally respond to some lifecycle hooks: currently
only `before-build` and `before-publish`.

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
(source/combine
  (files {:root "blog"})
  (files {:root "css"}))
```

or, to filter out all paths that aren't html:

```clojure
(source/construct
  (input other-source)
  (filter [path] (path/is-html? path)))
```

### Paths

Paths are individual pages of your site. They're composed of three
things:

- the pathname, such as "/about.html"
- metadata
- content

The metadata of a page usually includes some useful attributes like
the page title, its mime type, and a short name to refer to it using.

Ultimately, all of the metadata is thrown away when writing the path
to disk. Its primary purpose is to be used by transformations applied
at the Source and Path levels.

All paths conform to the `saison.proto/Path` protocol, but the
recommended way to access any data from a path is to use the
`pathname`, `metadata`, and `content` functions from the `saison.path`
namespace.

The `saison.path` namespace includes some tools for creating
transformations to paths. These transformations can change anything
about the path, and are created using the `deftransform` macro.

### Site Definition

Finally, the site definition is just a clojure map that wraps up a few
things:

- `:source`, the root source for the site
- `:env`, common configuration or data available to sources and paths
- `:output-to`, the folder where files will be written during build

The environment map can have anything you find useful in it. It will
be bound to the var `saison.path/*env*` when invoking the `metadata`
or `content` functions on any path.

The `:env` should contain the key `:public-url`, which will be used to
generate absolute URLs.

## Getting Started

TODO.

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
