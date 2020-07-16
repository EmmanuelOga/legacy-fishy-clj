(ns rainbowfish.main
  (:require [rainbowfish.httpd :as httpd]
            [rainbowfish.logging :as logging]
            [rainbowfish.jena :as jena]
            [rainbowfish.xmldb :as xmldb]))

(defn start
  []
  (logging/start)
  (xmldb/restart)
  (jena/restart)
  (httpd/restart))

(defn stop
  []
  (xmldb/stop)
  (jena/stop)
  (httpd/stop))
