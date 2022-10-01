(ns saison.source-test
  (:require [clojure.test :as t :refer [deftest is]]
            [saison.source :as sut]
            [saison.source.data :as data]
            [saison.path :as path]
            [saison.content :as content]))

(deftest concat-sources
  (let [src (sut/construct
             (data/source {:pathname "/index.html"
                           :content "index"}
                          {:pathname "/robots.txt"
                           :content "hi robots"})
             (data/source {:pathname "/stuff.md"
                           :content "stuff"}))
        outputs (sut/scan src)]
    (is (= 3 (count outputs)))))

(deftest concat-sources-handles-watching
  (let [watchers-1 (volatile! 0)
        watchers-2 (volatile! 0)
        fires (volatile! 0)
        source-1 (sut/construct
                  (sut/on-watch (fn [cb]
                                  (vswap! watchers-1 inc)
                                  (cb)
                                  (fn []
                                    (vswap! watchers-1 dec)))))
        source-2 (sut/construct
                  (sut/on-watch (fn [cb]
                                  (vswap! watchers-2 inc)
                                  (cb)
                                  (fn []
                                    (vswap! watchers-2 dec)))))
        merged (sut/construct source-1 source-2)]
    (is (zero? @watchers-1))
    (is (zero? @watchers-2))
    (is (zero? @fires))
    (let [close-fn (sut/watch merged (fn [] (vswap! fires inc)))]
      (is (= 1 @watchers-1))
      (is (= 1 @watchers-2))
      (is (= 2 @fires))
      (close-fn))
    (is (zero? @watchers-1))
    (is (zero? @watchers-2))
    (is (= 2 @fires))))

(deftest source-inputs-hooks
  (let [build-count (volatile! 0)
        publish-count (volatile! 0)
        s1 (sut/construct
            (sut/before-build-hook #(vswap! build-count inc)))
        s2 (sut/construct
            (sut/before-publish-hook #(vswap! publish-count inc)))
        merged (sut/construct s1 s2)]
    (sut/before-build merged)
    (is (= 1 @build-count))
    (is (zero? @publish-count))
    (sut/before-publish merged)
    (is (= 1 @build-count))
    (is (= 1 @publish-count))))

(deftest transform-paths-caches-on-object-identity
  (let [path-out (atom nil)
        content-fn (fn [] @path-out)
        build-path #(data/path {:pathname "a.html"
                                :content content-fn})
        current-path (atom (build-path))
        src (sut/construct
             (sut/modify-paths
              (fn [paths]
                (conj paths @current-path)))
             (sut/transform-paths (path/transformer)))
        set (fn [content]
              (reset! path-out content)
              (reset! current-path (build-path)))
        read-path #(path/find-by-path (sut/scan src) "a.html")
        read #(content/string (read-path))]
    ;; each build-path invocation should give a unique object that is
    ;; equal to the last. this mimics the behavior of saison.file.FilePath
    (is (not (identical? (build-path) (build-path))))
    (is (= (build-path) (build-path)))
    (set "abcd")
    (is (= "abcd" (read)))
    (set "abc")
    (is (= "abc" (read)))
    (set "abcd")
    (is (= "abcd" (read)))
    ;; repeat reads should yield identical objects
    (is (identical? (read-path) (read-path)))))

(deftest construction-and-environment
  (let [src (sut/construct
             (sut/when-previewing
              [[:env #(assoc % :hello true)]
               [:env #(assoc % :how "unknown")]
               [:env #(assoc % :n 42)]]
              (sut/emit 1
                        [(fn [] [7])]
                        (fn [] 8))
              [[:env #(update % :n inc)]
               [:env #(assoc % :goodbye true)]]
              (sut/emit 2))
             (sut/when-publishing
              (sut/emit 42)))]

    (is (= {:extra true, :hello true, :how "unknown", :n 43, :goodbye true}
           (sut/environment-for src {:extra true})))

    (binding [sut/*env* (sut/environment-for src {:publishing false})]
      (is (= [1 7 8 2] (sut/scan src))))

    (binding [sut/*env* (sut/environment-for src {:publishing true})]
      (is (= [42] (sut/scan src))))))
