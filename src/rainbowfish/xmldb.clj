(ns rainbowfish.xmldb
  (:require rainbowfish.xslt-factory)
  (:import
   [org.basex BaseXServer BaseXGUI]
   [org.basex.api.client ClientSession]))

(defonce
  ^{:doc "Instance of the BaseX DB server for the lifetime of the program"}
  server
  (do
    ; Inform JAXP APIs of rainbowfish's XSLT factory before calling
    ; BaseX code.
    (System/setProperty
      "javax.xml.transform.TransformerFactory",
      "rainbowfish.XsltFactory")
    ; Launch server.
    (BaseXServer. (make-array String 0))))

(defn launch-gui
  "The GUI doesn't take a context since it can connect to the local
  server with its own context."
  []
  (BaseXGUI. (make-array String 0)))
