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

(defn validation-errors
  [model shapes]
  (let [report (validate model shapes)]
    (->>
    (jena/values-of report :sh/result)
    (map (fn [m] (jena/map-of m [:sh/focusNode :sh/resultMessage :sh/resultPath :sh/value]))))))

(defn report-is-valid
  [report]
  (-> report (jena/value-of :sh/conforms) (.getBoolean)))

(let [shapes (jena/parse (io/resource "public/schema-org-shacle.ttl") "http://example.org" "TURTLE")
      data (jena/parse (io/resource "public/data.ttl") "http://example.org" "TURTLE")]
  [shapes data]
  (let [r (validation-errors data shapes)]
    (j/write-value-as-string r)))
