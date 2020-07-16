(ns rainbowfish.jena
  (:require [clojure.java.io :as io]
            [clojure.tools.logging :as log]
            [rainbowfish.config :as config]
            [rainbowfish.rdf :as rdf])
  (:import org.apache.jena.atlas.web.HttpException
           org.apache.jena.fuseki.main.FusekiServer
           [org.apache.jena.rdf.model Model ModelFactory]
           org.apache.jena.rdfconnection.RDFConnectionFuseki
           org.apache.jena.riot.RiotParseException
           org.apache.jena.riot.system.ErrorHandlerFactory
           org.apache.jena.tdb2.TDB2Factory))

;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;
;; IO
;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;

(defn create-empty-model
  []
  (ModelFactory/createDefaultModel))

(defn str-stream
  [str]
  (java.io.ByteArrayInputStream. (.getBytes str)))

(defn parse
  ([input base-url format]
   (let [model (create-empty-model)
         input-stream (io/input-stream input)]
     (.read model input-stream base-url format))))

(defn parse-string
  [input base-url format]
  ;; A bit messy but this ensures the error handler returns the line
  ;; and col. TODO: use a RDFParserBuilder.
  (ErrorHandlerFactory/setDefaultErrorHandler
   (ErrorHandlerFactory/errorHandlerDetailed))
  (try
    (parse (str-stream input) base-url format)
    (catch RiotParseException e
      {:level "error"
       :line (.getLine e)
       :column (.getCol e)
       :message (.getOriginalMessage e)})))

(defn write
  ([model] (write model "JSON-LD"))
  ([model format]
   (let [buffer (java.io.ByteArrayOutputStream.)]
     (.write model buffer format)
     (str buffer))))

(def ^:dynamic *max-print* 20)

; Extend print to understand Jena models.
(defmethod print-method Model [v ^java.io.Writer w]
  (let [graph (.getGraph v)
        sample (create-empty-model)
        size (.size graph)
        statements (iterator-seq (.listStatements v))]
    (.setNsPrefixes sample (.getPrefixMapping graph))
    (.write w (str "Jena, " size " statements."))
    (when (> size *max-print*)
      (.write w (str "Printing the first " *max-print* " triples.")))
    (.write w "\n\n")
    (run! (fn [s] (.add sample s)) (take *max-print* statements))
    (.write w (write sample "N3"))))

;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;
;; Access
;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;

(defn values-of
  [model prop]
  (-> (.listObjectsOfProperty model (rdf/prop prop)) iterator-seq))

(defn value-of
  [model prop]
  (-> (values-of model prop) first))

(defn map-of
  [model props]
  (let [extract
        (fn [prop]
          [prop
           (->> (rdf/prop prop)
                (.getProperty model)
                .getObject
                str)])]
    (into {} (map extract props))))

;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;
;; Remote connections
;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;

(defn build-conn
  [^String uri callback]
  (let [builder (doto (RDFConnectionFuseki/create) (.destination uri))]
    (with-open [built-conn (.build builder)]
      (try
        (callback built-conn)
        (catch HttpException e (log/info e))
        (catch Exception e (log/error e))))))

(defn fetch
  [^String uri ^String namedGraph]
  (build-conn uri (fn [conn] (.fetch conn namedGraph))))

(defn delete
  [^String uri ^String namedGraph]
  (build-conn uri (fn [conn] (.delete conn namedGraph))))

(defn put
  [^String uri ^String namedGraph ^Model model]
  (build-conn uri (fn [conn] (.put conn namedGraph model))))

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
     (let [config (config/config)]
       (when old-server (.stop old-server))

       (let [ds (TDB2Factory/createDataset (config :jena-path))
             fuseki (-> (FusekiServer/create)
                        (.add "/ds" ds)
                        (.build))]
         (.start fuseki)

         fuseki)))))
