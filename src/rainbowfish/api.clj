(ns rainbowfish.api
  "Implementation of the API methods."
  (:require [jsonista.core :as j]
            [rainbowfish.jena :as jena]
            [rainbowfish.topic :as t]
            [rainbowfish.xmldb :as xmldb]
            [ring.util.request :as req]
            [ring.util.response :as resp]))

(defn interpret-topic-response
  [basex-response]
  (let [[{:strs [code content-type]} payload] (xmldb/extract-parts basex-response)]
    (-> (if (and (>= code 200) (< code 300))
          (resp/response payload)
          (resp/bad-request payload))
        (resp/content-type content-type))))

(defn topic-get-or-default
  [{:keys [topic-name topic-graph rdf-server xmldb canonical]}]
  (let [meta-model (or (jena/fetch rdf-server topic-graph)
                       (jena/parse (xmldb/rf-path "API/default.ttl") canonical "TURTLE"))
        turtle (jena/write meta-model "TURTLE")]
    (xmldb/run-script
     "API/topic-get-or-init.xq"
     {:xmldb xmldb
      :topic-turtle turtle
      :topic-name topic-name})))

(defn topic-replace
  [{:strs [sdoc meta]}
   {:keys [topic-name topic-graph rdf-server xmldb canonical] :as request}]

  (let [sdoc-result (xmldb/run-script
                     "API/topic-validate.xq"
                     {:xmldb xmldb :topic-string sdoc})
        [{:strs [valid] :as opmeta} sdoc-validation] (xmldb/extract-parts sdoc-result)
        model (jena/parse-string meta canonical "TURTLE")
        jena-error (map? model)]
    (if (or (not valid) jena-error)
      (let [meta-validation {:meta-errors (if (map? model) [model] [])}
            payload (merge (j/read-value sdoc-validation) meta-validation)]
        (->
         (resp/bad-request (j/write-value-as-string payload))
         (resp/content-type "application/json")))
      (do
        (jena/put rdf-server topic-graph model)
        (xmldb/replace-doc xmldb topic-name sdoc)
        (->
         (topic-get-or-default request)
         (interpret-topic-response))))))

(defn topic-delete
  [{:keys [topic-name topic-graph rdf-server xmldb]}]
  (jena/delete rdf-server topic-graph)
  (xmldb/delete-doc xmldb topic-name)
  (->
   (resp/response (j/write-value-as-string
                   {:results (str "Deleted " topic-name)}))
   (resp/content-type "application/json")))

(defn topic
  "Performs different topic operations depending on HTTP method."
  [{:keys [request-method body route-match canonical] :as request}]
  (let [topic-name (get-in route-match [:path-params :topic-name])
        request (merge request (t/request-to-topic {:canonical canonical
                                                    :request-path (str topic-name)}))]
    (case request-method
      :get
      (->
       (topic-get-or-default request)
       (interpret-topic-response))

      :put
      (let [encoding (or (req/character-encoding request) "UTF-8")
            body-parsed (j/read-value (slurp body :encoding encoding))]
        (topic-replace body-parsed request))

      :delete
      (topic-delete request)

      (-> (resp/bad-request
           (j/write-value-as-string
            {:req-errors (str  "Unknown request " request-method)}
            (resp/content-type "application/json")))))))
