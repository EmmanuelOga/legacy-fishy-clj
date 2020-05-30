(ns rainbowfish.httpd
  (:require [ring.adapter.jetty :refer :all]
            [rainbowfish.http-app :as http]))

(def ^:dynamic options
  "Ring's run-jetty options"
  {:port 9876
   :join? false})

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
           (when old-server (.stop old-server))
           (run-jetty (http/app) options))))
