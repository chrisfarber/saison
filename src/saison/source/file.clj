(ns saison.source.file
  (:require [hawk.core :as hawk]
            [saison.proto :as proto]
            [saison.util :as util]
            [pantomime.mime :refer [mime-type-of]]))

(defrecord FilePath
           [file base-path path metadata]
  proto/Path
  (pathname [_] (util/add-path-component base-path path))
  (metadata [_]
    (let [known-mime (mime-type-of file)]
      (merge (when known-mime
               {:mime-type known-mime})
             metadata)))
  (content [_]
    file))

(defrecord FileSource
           [file-root base-path metadata
            cache]

  proto/Source
  (scan [_]
    (doseq [item (util/list-files file-root)]
      (let [[name f] item
            absolute-path (.getAbsolutePath f)]
        (when-not (get @cache absolute-path)
          (println "caching file:" absolute-path)
          (swap! cache assoc absolute-path
                 (map->FilePath {:file f
                                 :base-path base-path
                                 :path name
                                 :metadata metadata})))))
    (vals @cache))

  (watch
    [_ changed]
    (let [notifier (fn [_ e]
                     (let [f (:file e)
                           absolute-path (.getAbsolutePath f)]
                       (when-not (.isDirectory f)
                         (println "file changed:" absolute-path)
                         (swap! cache dissoc absolute-path)
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
  "create a source from files on the filesystem"
  [{:keys [root
           base-path
           metadata]}]
  (map->FileSource
   {:file-root root
    :base-path base-path
    :metadata metadata
    :cache (atom nil)}))
