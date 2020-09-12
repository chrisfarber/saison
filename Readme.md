# saison: static site generator

a static site generator.

## What saison does

My vision for Saison is roughly the following features:

- Live-reloading local preview
- For publishing, generate a static set of html, css, etc. Serve from
  any HTTP server.
- Easily write ClojureScript source
- Markdown processing
- Support the generation of RSS feeds from pages
- Allow for embedding of photos (or photo galleries) in a page.

## Running

To run, use the `saison.main` namespace. It can be launched using the
Clojure CLI tools:

```sh
clj -m saison.main -s my.site/config build
```

## Problems to solve

- What's the plan for photos and photo galleries?
- How do I automatically handle linking to posts?
