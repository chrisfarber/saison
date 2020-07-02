# saison: static site generator

a static site generator.

## What saison does

My vision for Saison is roughly the following features:

- Live-reloading local preview
- For publishing, generate a static set of html, css, etc. Serve from
  any HTTP server.
- Write content and pages in a format that is possible for Chloe to
  work with on her own.
- Support the generation of RSS feeds from pages
- Allow for embedding of photos (or photo galleries) in a page.

Stretch goals include

- Page-specific ClojureScript compilation
- Transformation of assets (e.g., resize images)

I envision the structure of a site will look like:

- `public/`: files in this folder will be copied directly into the
  generated site
- `pages/`: files in this folder will be compiled. This will generally
  involve processing via, say, markdown, and insertion into a parent
  template.
- `templates/`: this folder defines templates.

It probably should not be required that any particular page leverage a
template.

I'll also likely want it to be possible that we support a page or
template that is defined using logic or even a full programming language.

## What saison does not

No server side interactivity. No database.

## Problems to solve

- Can I dynamically discover and load Clojure files under `pages/`?
- What templating language(s) do I use?
- How can I compile page-specific ClojureScript on the fly?
- What's the plan for photos and photo galleries?
- How do I automatically handle linking to posts?

### Page resolution

How does Saison identify pages? Actually, what exactly is a page?

Here's my working idea. There is a top-level .edn file with an
arbitrary name. the name indicates the name of the static site that
can be generated. this allows for multiple to exist within the same
repository.

```edn
{:output "ehh"
:publish "ehh"
:asset-folder "public/"
:special-pages ['my-special.namespace/function]}
:pages [{:type 'something-}]
```

each function will be invoked with the page options map as well as the
configuration edn as a whole.

the set of paths can be computed by running through each item in
pages. they can return multiple paths for each page, but presumably
this is stable because it is generated from content on the file
system.

Before we even render any of the content, it should be possible to
detect whether there are any conflicting paths, whether dynamically
generated or simply static assets.

### Hoisting of meta information

A particular case that I don't think this design accounts for is the
embedding of the RSS URL into a top level page.

A quick and shoddy approach to this: simply hard code the URL into
however the actual index URL is generated.

To paper over this, it's possible that I could implement a scanner
that identifies local URLs in the generated files and matches them to
known paths.

### Automatic links to other paths

Given that the generate function will not be invoked until after all
paths are known, I think I have a solution for this as well.

Each page generator function, when invoked, can also receive the list
of all known pages with information about them. Each item will be the
same map that is output by the discovery function.

Additionally, we can fabricate a type that represents each static
file. Perhaps another approach altogether is to make this its own page
generator. This would increase the overall symmetry.

### Reusable components

- multiple slots, a-la svelte?
- backed by code and also css?

Idea: for all output that is html, parse the html. Use special
non-valid tags for components. Find these and replace with component
content.

perhaps I'm thinking about this all the wrong way. i should just
leverage enlive to do all the templating and then rely on custom
functions for more interesting pages?

it's still interesting to think about how the navigation case could
work. would it rely on each page that wants to use navigation links to
have knowledge of how to render them? is that bad?
