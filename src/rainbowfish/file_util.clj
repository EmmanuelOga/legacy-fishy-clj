(ns rainbowfish.file-util
  (:require [clojure.string :as s]
            [clojure.java.io :as io])
  (:import [java.nio.file Path Paths]))

(defn path-to-string
  "Normalizes the path (removing redundat section) and converts to a
  path with forward slashes"
  [^Path path]
  (-> path
      (.normalize)
      (.toString)
      (.replace "\\" "/")))

(defn relpath
  "Returns a path relative to the given root path."
  [root & paths]
  (->
   (Paths/get root (into-array String paths))
   (.toAbsolutePath)
   (path-to-string)))

(defn split-at-last
  "Returns prefix and postfix after splitting but last occurrence of
  given char."
  [str char]
  (if-let [idx (s/last-index-of str char)]
    [(subs str 0 idx) (subs str (+ 1 idx))]
    [str nil]))

(defn get-base-name-and-ext
  "Gets the base path, the name and the extension of the file under
  given path."
  [path]
  (->>
   (let [[pre1 post1] (split-at-last path "/")
         [pre2 post2] (split-at-last (or post1 pre1) ".")]
     (if (and pre2 post2)
       [pre1 pre2 post2]
       [pre1 (or pre2 post2) nil]))
   (map (fn [s] (if (empty? s) nil s)))))

(defn rm-rf [fname]
  "Deletes the file (recursively if it is a directory."
  (run! io/delete-file (-> (io/file fname) file-seq reverse )))

(defn file-to-path
  "Returns a Path given a file."
  ^Path [^java.io.File file]
  (Paths/get (.toURI file)))

(defn cp-r
  "Copy a file or directory to a given destination. If the src is a
  directory, it copies recursively."
  [& {:keys [src dst]}]
  (let [srcpath (Paths/get src (make-array String 0))
        files (->> (file-seq (io/file src))
                   (filter (fn [f] (.isFile f))))
        paths (map file-to-path files)]
    (run! (fn [p]
            (let [dstrel (.relativize srcpath p)
                  dst (io/file dst dstrel)]
              (io/make-parents dst)
              (io/copy (io/file p) (io/file dst))))
          paths)))
