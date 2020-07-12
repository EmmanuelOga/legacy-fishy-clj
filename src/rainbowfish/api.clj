(ns rainbowfish.api
  "Implementation of the API methods."
  (:require [jsonista.core :as j]
            [rainbowfish.jena :as jena]
            [rainbowfish.topic :as t]
            [rainbowfish.xmldb :as xmldb]
            [ring.util.request :as req]
            [ring.util.response :as resp]))

(defn topic-get-or-default
  [{:keys [topic-name topic-graph rdf-server xmldb]}]
  (let [fetch-model (jena/fetch rdf-server topic-graph)
        turtle (jena/write (or fetch-model (jena/create-empty-model)) "TURTLE")]
    (xmldb/run-script
     "API/topic-get-or-init.xq"
     {:xmldb xmldb
      :topic-turtle turtle
      :topic-name topic-name})))

(defn topic-replace
  [{:strs [sdoc meta]}
   {:keys [topic-name topic-graph xmldb canonical] :as request}]

  (let [raw-validation (xmldb/run-script
                        "API/topic-validate.xq"
                        {:xmldb xmldb :topic-string sdoc})
        [{:strs [valid] :as opmeta}] (xmldb/extract-parts raw-validation)]
    (if-not valid
      raw-validation
      (do
        ;; TODO: capture Jena validation errors.
        (let [model (jena/parse-string meta canonical "TURTLE")]
          (jena/put topic-graph model))

        (xmldb/replace-doc xmldb topic-name sdoc)
        (topic-get-or-default request)))))

(defn topic-delete
  [{:keys [topic-name topic-graph rdf-server xmldb]}]
  (jena/delete rdf-server topic-graph)
  (xmldb/delete-doc xmldb topic-name)
  (->
   (resp/response (j/write-value-as-string
                   {:results (str "Deleted " topic-name)}))
   (resp/content-type "application/json")))

(defn interpret-topic-response
  [basex-response]
  (let [[{:strs [code content-type]} payload] (xmldb/extract-parts basex-response)]
    (-> (if (and (>= code 200) (< code 300))
          (resp/response payload)
          (resp/bad-request payload))
        (resp/content-type content-type))))

(defn topic
  "Performs different topic operations depending on HTTP method."
  [{:keys [request-method body] :as request}]
  (let [request (merge request (t/request-to-topic request))]
    (case request-method
      :get
      (->
       (topic-get-or-default request)
       (interpret-topic-response))

      :put
      (let [encoding (or (req/character-encoding request) "UTF-8")
            body-parsed (j/read-value (slurp body :encoding encoding))]
        (->
         (topic-replace body-parsed request)
         (interpret-result)))

      :delete
      (topic-delete)

      (-> (resp/bad-request
           (j/write-value-as-string
            {:req-errors (str  "Unknown request " request-method)}
            (resp/content-type "application/json")))))))
