(ns rainbowfish.file-util
  (:require [clojure.string :as s])
  (:import java.nio.file.Paths))

(defn relpath
  "Returns a path relative to the given root path."
  [root & paths]
  (->
   (Paths/get root (into-array String paths))
   (.toAbsolutePath)
   (.normalize)
   (.toString)
   (.replace "\\" "/")))

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

