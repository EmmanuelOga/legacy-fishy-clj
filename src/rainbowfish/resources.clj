(ns rainbowfish.resources
  (:require [clojure.java.io :as io]
            [rainbowfish.file-util :as fu]))

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
        get-path (comp trim fu/path-to-string (fn [f] (.toPath f)))
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
