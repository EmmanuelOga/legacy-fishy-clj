(ns rainbowfish.public
  "Implementation of the providers for the public content of the sites."
  (:require [clojure.tools.logging :as log]
            [rainbowfish.file-util :as fu]
            [rainbowfish.jena :as jena]
            [rainbowfish.xmldb :as xmldb]
            [ring.util.request :as req]
            [ring.util.response :as resp])
  (:import rainbowfish.JenaClient))

(defn topic-get
  [topic content-type {:keys [named-graph] {:keys [xmldb cannonical]} :host-config}]
  (let [fetch-model (rainbowfish.JenaClient/fetch named-graph)
        json-ld (jena/write (or fetch-model (jena/create-empty-model)))]
    (xmldb/run-script
     (xmldb/rf-path "public/topic-get.xq")
     {:basepath (xmldb/rf-path ".")
      :xmldb xmldb
      :topic topic
      :json-ld json-ld
      :content-type content-type})))

(defn get-provider
  "Return a tuple `[provider content-type]` that knows how to return a
  response given a file format (extension)."
  [extension]
  ({"html" [topic-get "text/html"]
    "topic" [topic-get "application/xml"]} extension))

(defn handle-topic
  [{:keys [req host-config] {:keys [xmldb cannonical]} :host-config :as data}]
  (when (= :get (:request-method req))
    (let [[basename ext] (fu/path-to-topic (req/path-info req) "html")
          topic (str basename ".topic")
          named-graph (str "https://" cannonical "/" basename)]

      (when-let [[provider content-type] (get-provider ext)]
        (let [result
              (provider topic content-type (assoc data :named-graph named-graph))

              [{:strs [code] :as opmeta} payload]
              (xmldb/extract-parts result)]

          (log/info "public: GET topic"
                    :opmeta opmeta
                    :named-graph named-graph
                    :payload (str "[[[" (subs payload 0 255) " ...]]]"))

          (->
           (resp/response payload)
           (resp/content-type content-type)))))))
