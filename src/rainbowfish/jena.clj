(ns rainbowfish.jena
  (:require [rainbowfish.config :as config]
            [clojure.java.io :as io])
  (:import org.apache.jena.fuseki.main.FusekiServer
           org.apache.jena.query.DatasetFactory
           [org.apache.jena.rdf.model Model ModelFactory]))

(defn create-empty-model
  []
  (ModelFactory/createDefaultModel))

(defn str-stream
  [str]
  (java.io.ByteArrayInputStream. (.getBytes str)))

(defn parse-string
  ([input base-url format]
   (parse  (str-stream input) base-url format)))

(defn parse
  ([input base-url format]
   (let [model (create-empty-model)
         input-stream (io/input-stream input)]
     (.read model input-stream base-url format))))

(defn write
  ([model] (write model "JSON-LD"))
  ([model format]
   (let [buffer (java.io.ByteArrayOutputStream.)]
    (.write model buffer format)
    (str buffer))))

(def ^:dynamic *max-print* 500)

; Extend print to understand Jena models.
(defmethod print-method Model [v ^java.io.Writer w]
  (let [graph (.getGraph v)
        sample (create-empty-model)
        size (.size graph)
        statements (iterator-seq (.listStatements v))]
    (.setNsPrefixes sample (.getPrefixMapping graph) )
    (.write w (str "Jena, " size " statements."))
    (when (> size *max-print*)
      (.write w (str "Printing the first " *max-print* " triples.")))
    (.write w "\n\n")
    (run! (fn [s] (.add sample s)) (take *max-print* statements))
    (.write w (write sample "N3"))))

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
