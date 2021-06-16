(ns saison.source.file
  (:require [hawk.core :as hawk]
            [saison.path.caching :refer [cached]]
            [saison.proto :as proto]
            [saison.util :as util]
            [pantomime.mime :refer [mime-type-of]]
            [clojure.edn :as edn]
            [clojure.java.io :as io]
            [clojure.tools.logging :as log]))

(def metadata-file-suffix ".meta.edn")

(defn file-is-metadata [^java.io.File file]
  (-> file .getAbsolutePath (.endsWith metadata-file-suffix)))

(defn metadata-file-for [^java.io.File file]
  (java.io.File. (.getParent file)
                 (str (.getName file) metadata-file-suffix)))

(defn files-affected-by [^java.io.File file]
  (if (file-is-metadata file)
    [file
     (let [path-str (.getAbsolutePath file)]
       (io/file (.substring path-str 0 (- (count path-str)
                                          (count metadata-file-suffix)))))]
    [file]))

(defrecord FilePath
           [file base-path path metadata read-metadata-file]
  proto/Path
  (pathname [_] (util/add-path-component base-path path))
  (metadata [_]
    (let [known-mime (mime-type-of file)
          meta-file (and read-metadata-file
                         (metadata-file-for file))
          meta-exists (and meta-file (.exists meta-file))]
      (merge (when known-mime
               {:mime-type known-mime})
             metadata
             (when meta-exists
               (with-open [rdr (io/reader meta-file)
                           pb (java.io.PushbackReader. rdr)]
                 (edn/read pb))))))
  (content [_]
    file))

(defn eligible-files [file-root remove-metadata]
  (let [items (util/list-files file-root)]
    (if remove-metadata
      (filter (fn [[_ f]] ((complement file-is-metadata) f)) items)
      items)))

(defrecord FileSource
           [file-root base-path metadata
            cache read-metadata-files]

  proto/Source
  (scan [_]
    (doseq [item (eligible-files file-root read-metadata-files)]
      (let [[name f] item
            absolute-path (.getAbsolutePath f)]
        (when-not (get @cache absolute-path)
          (log/trace "caching file:" absolute-path)
          (swap! cache assoc absolute-path
                 (cached
                  ;; immediately wrap the path with (cached) so that it
                  ;; has a unique equality value. If we don't do this, then
                  ;; a source/transform-paths step will use cached data when
                  ;; it should not
                  (map->FilePath {:file f
                                  :base-path base-path
                                  :path name
                                  :metadata metadata
                                  :read-metadata-file read-metadata-files}))))))
    (vals @cache))

  (watch
    [_ changed]
    (let [notifier (fn [_ e]
                     (let [f (:file e)
                           absolute-path (.getAbsolutePath f)]
                       (when-not (.isDirectory f)
                         (log/trace "file event:" (:kind e) absolute-path)
                         (apply swap! cache dissoc
                                (map str (files-affected-by f)))
                         (changed))))
          watcher (hawk/watch! [{:paths [file-root]
                                 :handler notifier}])]
      (fn []
        (hawk/stop! watcher))))

  (start [_ _]
    (reset! cache nil))
  (stop [_ _]
    (reset! cache nil))

  (before-build-hook [_ _])
  (before-publish-hook [_ _]))

(defn files
  "create a source from files on the filesystem.
   
   the following options can be supplied:
   :root - required. the relative path of a folder to read files from.
   :base-path - if specified, prefix all pathnames with the base path
   :metadata - an optional map of data that will serve as default metadata
               for all paths discovered
   :parse-metadata - defaults to true. will read .meta.edn files as EDN
                     and attach their content as metadata"
  [{:keys [root
           base-path
           metadata
           parse-metadata]
    :or {parse-metadata true}}]
  (map->FileSource
   {:file-root root
    :base-path base-path
    :metadata metadata
    :cache (atom nil)
    :read-metadata-files parse-metadata}))
