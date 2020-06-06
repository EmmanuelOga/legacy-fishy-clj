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

(defn get-base-and-ext
  "Gets the basename of the file and its extension."
  [path]
  (->>
   (let [[pre1 post1] (split-at-last path "/")
         [pre2 post2 :as parts] (split-at-last (or post1 pre1) ".")]
     (if (and pre2 post2) parts [(or pre2 post2) nil]))
   (map (fn [s] (if (empty? s) nil s)))))

(defn rm-rf [fname]
  "Deletes the file (recursively if it is a directory."
  (run! io/delete-file (-> (io/file fname) file-seq reverse )))

(defn file-to-path
  "Returns a Path given a file."
  ^Path [^java.io.File file]
  (Paths/get (.toURI file)))

(defn replace
  "Copy a file or directory to a given destination. Will recursively
  delete everything at destination and then copy the files from the
  source to the destionation."
  [& {:keys [src dst]}]
  (let [srcpath (Paths/get src (make-array String 0))
        files (->> (file-seq (io/file src))
                   (filter (fn [f] (.isFile f))))
        paths (map file-to-path files)]
    (rm-rf dst)
    (run! (fn [p]
            (let [dstrel (.relativize srcpath p)
                  dst (io/file dst dstrel)]
              (io/make-parents dst)
              (io/copy (io/file p) (io/file dst))))
          paths)))


;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;
;; Manifests.
;; Create a manifest.edn file on a given resources/ folder so we can get the
;; list of files without having to run nasty reflection tricks.
;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;

(defn get-resources-manifest
  "NOTE: only works on source folder, not from JAR.
  Creates a list of the files on the resources folder. This is easier
  to work with than trying to use Java APIs to list the files on the
  folder... the idea is to generate this file during development."
  [path]
  (let [trim (fn [s] (.replace s "resources/" ""))
        files  (->> (file-seq (io/file path))
                    (filter (fn [f] (.isFile f))))
        get-path (comp trim path-to-string (fn [f] (.toPath f)))
        mpath (trim (str path "manifest.edn"))]
    (filter (fn [p] (not= p mpath)) (map get-path files))))

(defn create-resources-manifest
  "Writes a manifest.edn file on the given resources path (must end in
  slash). Ex: (create-resources-manifest \"resources/assets/\")"
  [path]
  (spit (str path "manifest.edn")
        (pr-str (get-resources-manifest path))))

(defn read-resources-manifest
  "Reads a manifest.edn file on the given resources path (must end in
  slash)"
  [path]
  (read-string (slurp (str path "manifest.edn"))))
