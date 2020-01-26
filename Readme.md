# saison: static site generator

First, why am I making this?

I would like to have a static site generator with which I can build a
few things:

* A landing page for my personal domain
* A family website with photos
* Eventually, perhaps my own blog

It's probably a wise choice to choose an existing tool to accomplish
this. Likely, it would also be easier. In particular, next.js seems
like a very solid choice because it's a mix of familiar technologies
(React, TypeScript) and because it's actively being used on projects
at Atomic.

Despite this, I seem to be making a less efficient choice.

Rather than simply get up and running with next.js, I'm instead
leaning toward another option. My plan is to create an old-style
static site generator in Clojure. This is, after all, a project for my
own personal use with time that's been appropriated from other areas
of my life. Rather than writing more of the same exact thing I do at
work, I'd like to use this time to use tools that I enjoy more.

Another concern is that, while next.js is popular and actively
maintained now, I don't know how much churn there will be in the
future. If I create a website with next.js, and leave it alone for two
years, will I have a huge task on my hand to "modernize" it? I do not
want that.

This is an explicit trade-off: in rolling my own, I'm foregoing some
more advanced features of other tools that I will likely not have the
time to reimplement on my own. Instead, I'm choosing a stable
ecosystem that won't change out from underneath me, at the expense of
more advanced capabilities.

## What does saison do?

My vision for Saison is roughly the following features:

* Live-reloading preview
* For publishing, generate a static set of html, css, etc. Serve from
  any HTTP server.
* Write content and pages in a format that is possible for Chloe to
  work with on her own.
* Support the generation of RSS feeds from pages
* Allow for embedding of photos (or photo galleries) in a page.

Stretch goals include
* Page-specific ClojureScript compilation
* Transformation of assets (e.g., resize images)

I envision the structure of a site will look like:
* `public/`: files in this folder will be copied directly into the
  generated site
* `pages/`: files in this folder will be compiled. This will generally
  involve processing via, say, markdown, and insertion into a parent
  template.
* `templates/`: this folder defines templates.

It probably should not be required that any particular page leverage a
template.

I'll also likely want it to be possible that we support a page or
template that is defined using logic or even a full programming language.

## What does Saison not do?

No server side interactivity. No database.

## Problems to solve

* Can I dynamically discover and load Clojure files under `pages/`?
* What templating language(s) do I use?
* How can I compile page-specific ClojureScript on the fly?
* What's the plan for photos and photo galleries?
* How do I automatically handle linking to posts?

### Page resolution

How does Saison identify pages? Actually, what exactly is a page?

Here's my working idea. There is a top-level .edn file with an
arbitrary name. the name indicates the name of the static site that
can be generated. this allows for multiple to exist within the same
repository.

```
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

### Hoisting of meta information?

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

