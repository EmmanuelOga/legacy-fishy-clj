(ns rainbowfish.xmldb
  (:require [clojure.java.io :as io]
            [rainbowfish.config :as config]
            [rainbowfish.file-util :as fu]
            [clojure.string :as s])
  (:import [org.basex BaseXGUI BaseXServer]
           org.basex.api.client.ClientSession))

(def rf-xmldb-name
  "Name of the XML database used to hold all assets."
  "rainbowfish")

(defn ^:dynamic options
  "BaseX Database Options"
  []
  {:root (:basex-path (config/config))
   :host "localhost"
   :port 1984
   :user "admin"
   :password "admin"})

(defn assets-path
  "Returns the path where basex stores static files"
  []
  (fu/relpath (:root (options)) "data" rf-xmldb-name "raw"))

(defonce
  ^{:doc "Instance of the BaseX DB server for the lifetime of the program"}
  server
  (do
    (System/setProperty "org.basex.path" (:root (options)))
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

(defn create-session
  "Creates a network session to talk to BaseX server"
  []
  (let [o (options)]
    (ClientSession. (:host o) (:port o) (:user o) (:password o))))

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

