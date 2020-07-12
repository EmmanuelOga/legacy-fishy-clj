(ns rainbowfish.httpd
  (:require [rainbowfish.config :as config]
            [rainbowfish.http-app :as http]
            [ring.adapter.jetty :refer :all]))

(defonce
  ^{:doc "Instance of the HTTPD for the lifetime of the program."}
  server (atom nil))

(defn stop []
  (swap!
   server
   (fn [old-server]
     (when old-server (.stop old-server))
     nil)))

(defn restart []
  (swap!
   server
   (fn [old-server]
     (let [options (assoc (:backend (config/config)) :join? false)]
       (when old-server (.stop old-server))
       (run-jetty (http/app) options)))))
