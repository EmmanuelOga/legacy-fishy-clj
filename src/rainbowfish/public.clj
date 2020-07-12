(ns rainbowfish.public
  "Implementation of the providers for the public content of the sites."
  (:require [rainbowfish.jena :as jena]
            [rainbowfish.topic :as t]
            [rainbowfish.xmldb :as xmldb]
            [ring.util.response :as resp]
            [clojure.tools.logging :as log]))

(defn topic-get
  [{:keys [rdf-server xmldb topic-name topic-graph topic-content-type] :as request}]
  (log/info "topic-get" [rdf-server xmldb topic-name topic-graph topic-content-type])
  (let [json-ld (jena/write (or (jena/fetch rdf-server topic-graph)
                                (jena/create-empty-model)))]
    (xmldb/run-script
     "public/topic-get.xq"
     {:xmldb xmldb
      :topic-name topic-name
      :topic-json-ld json-ld
      :topic-content-type topic-content-type})))

(defn handle-topic
  [{:keys [request-method] :as request}]
  (when (= request-method :get)
    (let [{:keys [topic-content-type] :as topic-data}
          (t/request-to-topic request)
          [result payload]
          (xmldb/extract-parts (topic-get (merge request topic-data)))]
      (-> (resp/response payload)
          (resp/content-type topic-content-type)))))
