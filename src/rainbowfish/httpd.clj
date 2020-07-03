(ns rainbowfish.httpd
  (:require [ring.adapter.jetty :refer :all]
            [rainbowfish.config :as config]
            [rainbowfish.http-app :as http]))

(defonce
  ^{:doc "Instance of the HTTPD for the lifetime of the program."}
  server (atom nil))

(defn stop []
  (swap! server
         (fn [old-server]
           (when old-server (.stop old-server))
           nil)))

(defn restart []
  (swap! server
         (fn [old-server]
           (let [options (assoc (:backend (config/config)) :join? false)]
             (println options)
             (when old-server (.stop old-server))
             (run-jetty (http/app) options)))))
