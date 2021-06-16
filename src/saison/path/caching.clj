(ns saison.path.caching
  (:require [saison.proto :as proto]
            [saison.content :as content]))

(defn cache-get
  ([atom key miss-fn]
   (cache-get atom key miss-fn (constantly true)))
  ([atom key miss-fn cacheable-fn]
   (if-let [hit (get @atom key)]
     hit
     (let [val (miss-fn)]
       (when (cacheable-fn val)
         (swap! atom assoc key val))
       val))))

(defn content-to-cache [content]
  (cond (instance? java.io.File content)
        nil

        (isa? (type content) ::content/streamable)
        (with-open [s (content/input-stream content)]
          (.readAllBytes s))

        :else content))

(defn cache-to-content [cached-content]
  (if (bytes? cached-content)
    (java.io.ByteArrayInputStream. cached-content)
    cached-content))

(defrecord CachingPath
           [original cache]

  proto/Path
  (pathname [_]
    (cache-get cache :pathname
               #(proto/pathname original)))

  (metadata [_]
    (cache-get cache :metadata
               #(proto/metadata original)))

  (content [_]
    (let [cached-content (cache-get cache :content
                                    #(content-to-cache (proto/content original))
                                    #(not (instance? java.io.File %)))]
      (if cached-content
        (cache-to-content cached-content)
        (proto/content original)))))

(defn cached
  "Wrap a path so that its pathname, metadata, and content will be cached.
   Content represented as `java.io.File` objects will not be cached."
  [path]
  (map->CachingPath {:original path
                     :cache (atom {})}))