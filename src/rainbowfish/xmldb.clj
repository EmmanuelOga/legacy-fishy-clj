(ns rainbowfish.xmldb
  (:require [clojure.java.io :as io]
            [rainbowfish.cli :as cli]
            rainbowfish.xslt-factory)
  (:import [org.basex BaseXGUI BaseXServer]
           org.basex.api.client.ClientSession
           org.basex.core.cmd.Add))

(def ^:dynamic options
  "BaseX Database Options"
  {:root (cli/relpath "data/xmldb")
   :host "localhost"
   :port 1984
   :user "admin"
   :password "admin"})

(defonce
  ^{:doc "Instance of the BaseX DB server for the lifetime of the program"}
  server
  (do
    (System/setProperty "org.basex.path" (:root options))
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
           (BaseXServer. (into-array [(str "-p" (:port options))])))))

(defn launch-gui
  "The GUI doesn't take a context since it can connect to the local
  server with its own context."
  []
  (BaseXGUI. (make-array String 0)))

(defn create-session
  "Creates a network session to talk to BaseX server"
  []
  (let [o options]
    (ClientSession. (:host o) (:port o) (:user o) (:password o))))

(defn open
  "Opens a BaseX session and calls the callback with it"
  [callback]
  (with-open [session (create-session)]
    (callback session)))

(defn fire
  "Opens a BaseX session and runs a single string command"
  [& args]
  (open (fn [sess] (.execute sess (apply str args)))))

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
                     (.bind query varname value typename)) bindings)
             (callback query sess))))))

