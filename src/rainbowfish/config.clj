(ns rainbowfish.config
  (:require [clojure.edn :as edn]
            [clojure.java.io :as io]
            [rainbowfish.file-util :as fu]))

(def cli-params
  "Configuration that is expected to come from the CLI/Startup"
  (atom {:dev-mode true}))

(defn expand-config
  "Given the config path and the result of reading the EDN at that file,
  expands a few fields that are derived from the config."
  [config-path {:keys [basex-path jena-path sites backend] :as config-from-file}]
  (let [flatten-site
        (fn [{:keys [path xmldb hosts default-author canonical]}]
          (let [assets-path (fu/relpath config-path ".." path)
                host-config {:default-author default-author
                             :canonical canonical
                             :assets-path assets-path
                             :xmldb xmldb}]
            (map (fn [host] [host host-config]) hosts)))

        hosts
        (->> (mapcat flatten-site sites)
             (apply concat)
             (apply hash-map))]
    (merge
     ;; Basex path is relative to config file.
     ;; Parameters from sites are used to compute hosts.
     ;; Keep every other setting on the file as it was.
     (dissoc config-from-file :sites :basex-path :jena-path)
     {:basex-path (fu/relpath config-path ".." basex-path)
      :jena-path (fu/relpath config-path ".." jena-path)
      :hosts hosts}
     @cli-params)))

(defn read-config
  "Attempts to create configuration by openining the .edn at the given path."
  [config-path]
  (if (.exists (io/file config-path))
    (expand-config
     config-path
     (edn/read-string (slurp config-path)))
    {}))

(defn ^:dynamic config
  []
  (read-config "sites/rainbowfish.edn"))
