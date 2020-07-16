(ns rainbowfish.shacl
  (:import org.topbraid.shacl.validation.ValidationUtil)
  (:require [rainbowfish.jena :as jena]
            [rainbowfish.rdf :as rdf]
            [clojure.java.io :as io]
            [jsonista.core :as j]))

(defn validate
  ([model]
   (let [report (ValidationUtil/validateModel model model true)]
     (.getModel report)))
  ([model shapes]
   (let [report (ValidationUtil/validateModel model shapes true)
         report-model (.getModel report)]
     (.setNsPrefix report-model "sh" (:sh rdf/uri)))))

(let [shapes (jena/parse (io/resource "public/schema-org-shacle.ttl") "http://example.org" "TURTLE")
      data (jena/parse (io/resource "public/data.ttl") "http://example.org" "TURTLE")]
  [shapes data]
  (->
   (validate data shapes)
   (jena/write)
   (j/read-value (j/object-mapper {:decode-key-fn keyword}))))
