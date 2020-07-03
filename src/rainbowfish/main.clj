(ns rainbowfish.main
  (:require [rainbowfish.httpd :as httpd]
            [rainbowfish.logging :as logging]
            [rainbowfish.xmldb :as xmldb]))

(defn start
  []
  (logging/start)
  (xmldb/restart)
  (httpd/restart))
