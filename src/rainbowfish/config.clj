(ns rainbowfish.config
  (:require [clojure.java.io :as io]
            [clojure.edn :as edn]
            [rainbowfish.file-util :as fu]))

(defn expand-config
  "Given the config path and the result of reading the EDN at that file,
  expands a few fields that are derived from the config."
  [config-path {:keys [basex-path sites] :as config}]
  (let [flatten-site (fn [{:keys [path xmldb hosts]}]
                       (map (fn [host]
                              [host {:assets-path (fu/relpath config-path ".." path "static")
                                     :xmldb xmldb}]) hosts))
        hosts (->> (map flatten-site sites) ; Produces as many elems as hosts per site
                   (apply concat) ; Flattens into a single list [[host data] ... ]
                   (apply concat) ; Flattens
                   (apply hash-map))]
    {:basex-path (fu/relpath config-path basex-path)
     :hosts hosts}))

(defn read-config
  "Attempts to create configuration by openining the .edn at the given path."
  [config-path]
  (if (.exists (io/file config-path))
    (expand-config
     config-path
     (edn/read-string (slurp config-path)))
    {}))

(defn ^:dynamic config
  "Process wide configuration"
  []
  (read-config "sites/rainbowfish.edn"))
