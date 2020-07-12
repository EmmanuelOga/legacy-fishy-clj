(ns rainbowfish.xmldb
  (:require [clojure.string :as str]
            [jsonista.core :as j]
            [rainbowfish.config :as config]
            [rainbowfish.file-util :as fu])
  (:import [org.basex BaseXGUI BaseXServer]
           org.basex.api.client.ClientSession
           [org.basex.core.cmd Add Replace Delete]))

(defn ^:dynamic options
  "BaseX Database Options"
  []
  {:host "localhost"
   :port 1984
   :user "admin"
   :password "admin"})

(defn basex-path
  "Returns the BaseX data path."
  []
  (:basex-path (config/config)))

(defn rf-path
  "Returns a path relative to the basex-path."
  [path]
  (fu/relpath (basex-path) "rainbowfish" path))

(defn create-session
  "Creates a network session to talk to BaseX server"
  []
  (let [{:keys [host port user password]} (options)]
    (ClientSession. host port user password)))

(defn open
  "Opens a BaseX session and calls the callback with it"
  [callback]
  (with-open [session (create-session)]
    (callback session)))

(defn fire
  "Opens a BaseX session and runs every command given"
  [& commands]
  (open (fn [sess]
          (let [exec (fn [cmd] (.execute sess cmd))
                last (last commands)]
            (when last
              (run! exec (butlast commands))
              (exec last))))))

(defn query
  "Runs a query.
  `(query
    \"...xquery...\"
    [[\"$extern-name\" value \"xs:string\"]]
    (fn [q] (.bind q \"$var\" \"42\" \"xs:integer\")
            (.execute q)))`"
  ([xq]
   (query xq [] (fn [q sess] (.execute q))))
  ([xq bindings]
   (query xq bindings (fn [q sess] (.execute q))))
  ([xq bindings callback]
   (open (fn [sess]
           (with-open [query (.query sess xq)]
             ; Bind any parameters.
             (run! (fn [[varname value typename]]
                     (.bind query
                            varname
                            value
                            (or typename "xs:string"))) bindings)
             (callback query sess))))))

(defn run-script
  [rel-path bindings-map]
  (let [escape-kv (fn [[k v]]
                    (str "$" (name k) "=" (str/replace v "," ",,")))
        ;; User provided bindings plus the default ones.
        all-bindings (merge {:basepath (rf-path ".")} bindings-map)
        bindings (str/join ", " (map escape-kv all-bindings))
        exec (fn [sess cmd]
               (.execute sess cmd))]
    (open (fn [sess]
            (when-not (empty? bindings)
              (exec sess (str "SET BINDINGS " bindings)))
            (exec sess (str "RUN " (rf-path rel-path)))))))

(defn delete-doc
  [xmldb path]
  (fire
   (str "OPEN " xmldb)
   (Delete. path)))

(defn add-doc
  [xmldb path doc]
  (fire
   (str "OPEN " xmldb)
   "SET CHOP false"
   "SET SERIALIZER indent=no"
   (Add. path doc)))

(defn replace-doc
  [xmldb path doc]
  (fire
   (str "OPEN " xmldb)
   "SET CHOP false"
   "SET SERIALIZER indent=no"
   (Replace. path doc)))

(defn extract-parts
  [basex-resp]
  (let [[first rest] (str/split basex-resp #"===BOUNDARY===" 2)]
    [(j/read-value first) rest]))

;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;
;; Server
;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;

(defonce
  ^{:doc "Instance of the BaseX DB server for the lifetime of the program"}
  server
  (do
    (System/setProperty "org.basex.path" (basex-path))
    ; Inform JAXP APIs of rainbowfish's XSLT factory before calling
    ; BaseX code.
    (System/setProperty
     "javax.xml.transform.TransformerFactory",
     "rainbowfish.XsltFactory")
    (atom nil)))

(defn stop
  "Stops the BaseX server, if it is running."
  []
  (swap! server
         (fn [old-server]
           (when old-server (.stop old-server))
           nil)))

(defn restart
  "Stops the server if there is one running, and starts it again"
  []
  (swap! server
         (fn [old-server]
           (when old-server (.stop old-server))
           (BaseXServer. (into-array [(str "-p" (:port (options)))]))))

  ; Ensures the DBs of every site exist.
  (let [db-names (distinct (map :xmldb (vals (:hosts (config/config)))))]
    (run! (fn [dbn] (fire (str "CHECK " dbn))) db-names)))

(defn ensure-running
  "Runs the database if it is not running already."
  []
  (if (not @server) (restart)))

(defn launch-gui
  "The GUI doesn't take a context since it can connect to the local
  server with its own context."
  []
  (BaseXGUI. (make-array String 0)))
