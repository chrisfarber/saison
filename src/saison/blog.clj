(ns saison.blog
  (:require [clojure.data.xml :as xml]
            [saison.content :as content]
            [saison.content.html :as htmlc]
            [saison.content.xml]
            [saison.path :as path]
            [saison.source :as source]
            [saison.source.data :as data]
            [saison.util :as util]
            [tick.core :as t]))

(defn- feed? [{:keys [feed-id]}]
  (fn [path]
    (and
     (= feed-id (:feed-id (path/metadata path)))
     (path/html? path))))

(defn- sorted-blog-entries [paths pred]
  (let [matching (filter pred paths)]
    (sort-by #(:created-at (path/metadata %))
             (comp - compare)
             matching)))

(defn most-recent-date [paths]
  (let [dates (filter some?
                      (flatten (map (fn [path]
                                      (let [meta (path/metadata path)]
                                        [(:created-at meta)
                                         (:published-at meta)]))
                                    paths)))]
    (when (not-empty dates)
      (apply t/max dates))))

(defn get-content [path selector?]
  (let [content (path/content path)]
    (if selector?
      (htmlc/select content selector?)
      content)))

(defn paths-in-feed
  "Find current paths that belong in the feed, and sort them
  by creation date (`:created-at`) (descending)."
  [paths feed-id]
  (sorted-blog-entries paths (feed? {:feed-id feed-id})))

(defn- element-for-item [path public-url content-selector]
  (let [metadata (path/metadata path)
        {:keys [title
                created-at
                published-at]} metadata
        pathname (path/pathname path)
        updated-at (or published-at created-at)
        url (util/append-url-component public-url pathname)]
    [:entry nil
     [:title nil title]
     [:link {:href url}]
     [:id nil url]
     [:updated nil (util/rfc3339 updated-at)]
     (when published-at
       [:published nil (util/rfc3339 published-at)])
     [:content {:type "html"} (content/string (get-content path content-selector))]]))

(defn- compile-atom-feed [opts entries]
  (let [{:keys [feed-path
                feed-title
                feed-icon
                feed-items
                content-selector
                author-name
                author-email]
         :or {feed-items 10}} opts
        public-url (:public-url source/*env*)
        feed-url (util/append-url-component public-url feed-path)
        entries-to-include (take feed-items entries)
        items (map #(element-for-item % public-url content-selector) entries-to-include)
        update-date (most-recent-date entries-to-include)]
    (xml/sexp-as-element
     [:feed {:xmlns "http://www.w3.org/2005/Atom"}
      (when feed-title
        [:title nil feed-title])
      (when (or author-name author-email)
        [:author nil
         (when author-name
           [:name nil author-name])
         (when author-email
           [:email nil author-email])])
      (when feed-icon
        [:icon nil (util/append-url-component public-url feed-icon)])
      [:id nil feed-url]
      (when update-date
        [:updated nil (util/rfc3339 update-date)])
      [:generator {:uri "https://github.com/chrisfarber/saison"} "saison"]
      [:link {:rel "self" :href feed-url}]
      [:link {:rel "alternate" :href public-url}]
      items])))

(defn- build-feed [paths opts]
  (let [{:keys [feed-path
                feed-id]} opts]
    (data/path {:pathname feed-path
                :metadata {:mime-type "application/xml"
                           :alias (str "feed-" feed-id)}
                :content (fn []
                           (let [entries (sorted-blog-entries paths (feed? opts))]
                             (compile-atom-feed opts entries)))})))

(defn- add-feed [paths opts]
  (conj paths (build-feed paths opts)))

(defn feed
  "create a blog using the supplied options.

  opts can contain:
  - :feed-path, the path of the atom xml feed to emit
  - :feed-id, a string id for the blog. other paths having this feed-id on their
              metadata will be included in the feed.
  - :feed-title, the <title> of the atom feed
  - :feed-icon, a path to the icon to use for the feed
  - :feed-items, the number of items to include. default 10.
  - :content-selector, an enlive selector that will be used to extract the content
  - :author-name
  - :author-email"
  [opts]
  (let [{:keys [feed-path]} opts]
    (source/modify-paths
     (fn [paths]
       (cond-> paths
         (some? feed-path) (add-feed opts))))))
