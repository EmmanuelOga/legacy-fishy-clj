(ns rainbowfish.jena
  (:require [rainbowfish.config :as config])
  (:import org.apache.jena.fuseki.main.FusekiServer
           org.apache.jena.query.DatasetFactory))

;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;
;; Server
;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;

(defonce
  ^{:doc "Instance of the Jena server for the lifetime of the program."}
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
     (let [options (config/config)]
       (when old-server (.stop old-server))

       (let [ds (DatasetFactory/createTxnMem)
             fuseki (-> (FusekiServer/create)
                        (.add "/ds" ds)
                        (.build))]
         (.start fuseki)

         fuseki)))))
