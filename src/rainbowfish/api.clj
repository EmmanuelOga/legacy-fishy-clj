(ns rainbowfish.api
  "Implementation of the API methods."
  (:require [clojure.java.io :as io]
            [jsonista.core :as j]
            [rainbowfish.file-util :as fu]
            [rainbowfish.xmldb :as xmldb]
            [ring.util.request :as request]
            [ring.util.response :as resp]))

(defn topic-complete
  [topic
   {{:keys [request-method]} :req
    {:keys [path-params]} :match
    {:keys [xmldb]} :host-config}]
  (-> "<wip/>"
      (resp/response)
      (resp/content-type "application/xml")))

(defn topic-get-or-default
  [topic {{:keys [xmldb]} :host-config}]
  (xmldb/run-script
   (xmldb/rf-path "API/topic-get-or-init.xq")
   {:basepath (xmldb/rf-path ".")
    :xmldb xmldb
    :topic topic}))

(defn topic-replace
  [topic
   {:strs [sdoc meta]}
   {{:keys [xmldb]} :host-config :as data}]

  (let [raw-validation (xmldb/run-script
                        (xmldb/rf-path "API/topic-validate.xq")
                        {:xmldb xmldb
                         :topic ""
                         :basepath (xmldb/rf-path ".")
                         :topic-string sdoc})
        [{:strs [valid]}] (xmldb/extract-parts raw-validation)]
    (if valid
      (do
        (xmldb/replace-doc xmldb (str "/" topic) sdoc)
        (topic-get-or-default topic data))
      raw-validation)))

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
    {:keys [xmldb]} :host-config :as data}]
  (let [[topic _] (fu/path-to-topic (:key path-params))]
    (case request-method
      :get
      (->
       (topic-get-or-default topic data)
       (interpret-result))

      :delete
      (-> "<delete/>"
          (resp/response)
          (resp/content-type "application/xml"))

      :put
      (let [encoding (or (request/character-encoding req) "UTF-8")
            body-parsed (j/read-value (slurp body :encoding encoding))]
        (->
         (topic-replace topic body-parsed data)
         (interpret-result)))

      (-> (resp/bad-request
           (str "<error>Unknown request: " request-method "</error>"))
          (resp/content-type "application/xml")))))
