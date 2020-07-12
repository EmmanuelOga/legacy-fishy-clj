(ns rainbowfish.httpd
  (:require [rainbowfish.config :as config]
            [rainbowfish.http-app :as http]
            [ring.adapter.jetty :refer [run-jetty]]))

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
     (let [{:keys [http-api-port]} (config/config)]
       (when old-server (.stop old-server))
       (run-jetty
        (http/app)
        {:port http-api-port :join? false})))))
