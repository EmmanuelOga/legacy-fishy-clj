(ns rainbowfish.api
  "Implementation of the API methods."
  (:require [clojure.tools.logging :as log]
            [jsonista.core :as j]
            [rainbowfish.file-util :as fu]
            [rainbowfish.xmldb :as xmldb]
            [ring.util.request :as request]
            [ring.util.response :as resp]
            [rainbowfish.jena :as jena]))

(defn topic-complete
  [topic
   {{:keys [request-method]} :req
    {:keys [path-params]} :match
    {:keys [xmldb]} :host-config}]
  (-> "<wip/>"
      (resp/response)
      (resp/content-type "application/xml")))

(defn topic-get-or-default
  [topic {:keys [named-graph] {:keys [xmldb]} :host-config}]
  (let [fetch-model (rainbowfish.JenaClient/fetch named-graph)
        json-ld (jena/write (or fetch-model (jena/create-empty-model)) "TURTLE")]
    (xmldb/run-script
     (xmldb/rf-path "API/topic-get-or-init.xq")
     {:basepath (xmldb/rf-path ".")
      :xmldb xmldb
      :turtle json-ld
      :topic topic})))

(defn topic-replace
  [topic
   {:strs [sdoc meta]}
   {:keys [named-graph] {:keys [xmldb cannonical]} :host-config :as data}]

  (let [raw-validation (xmldb/run-script
                        (xmldb/rf-path "API/topic-validate.xq")
                        {:xmldb xmldb
                         :topic ""
                         :basepath (xmldb/rf-path ".")
                         :topic-string sdoc})
        [{:strs [valid] :as opmeta}] (xmldb/extract-parts raw-validation)]
    (if valid
      (do
        ;; TODO: capture Jena validation errors.
        (let [model (jena/parse-string meta cannonical "TURTLE")]
          (rainbowfish.JenaClient/put named-graph model))

        (xmldb/replace-doc xmldb topic sdoc)
        (topic-get-or-default topic data))
      raw-validation)))

(defn topic-delete
  [topic
   {{:keys [xmldb]} :host-config :as data}]
  (xmldb/delete-doc xmldb topic)
  (->
   (resp/response (j/write-value-as-string {:result (str "Deleted " topic)}))
   (resp/content-type "application/json")))

(defn interpret-result
  [basex-response]
  (let [[{:strs [code content-type]} payload]
        (xmldb/extract-parts basex-response)]
    (-> (if (and (>= code 200) (< code 300))
          (resp/response payload)
          (resp/bad-request payload))
        (resp/content-type content-type))))

(defn topic
  "Performs different topic operations depending on HTTP method."
  [{:keys [req]
    {:keys [request-method body]} :req
    {:keys [path-params]} :match
    {:keys [xmldb cannonical]} :host-config :as data}]
  (let [[basename ext] (fu/path-to-topic (:key path-params) "topic")
        topic (str basename "." ext)
        named-graph (str "https://" cannonical "/" basename)

        ;; Overwrite data.
        data (assoc data :named-graph named-graph)]
    (case request-method
      :get
      (->
       (topic-get-or-default topic data)
       (interpret-result))

      :delete
      (topic-delete topic data)

      :put
      (let [encoding (or (request/character-encoding req) "UTF-8")
            body-parsed (j/read-value (slurp body :encoding encoding))]
        (->
         (topic-replace topic body-parsed data)
         (interpret-result)))

      (-> (resp/bad-request
           (str "{ \"error\" : \"Unknown request" request-method "\" }"))
          (resp/content-type "application/json")))))
