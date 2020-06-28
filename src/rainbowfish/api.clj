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
   {:basepath (xmldb/rf-path ".") :xmldb xmldb :topic topic}))

(defn topic-replace
  [topic body {{:keys [xmldb]} :host-config}]
  (let [json (j/read-value body)
        sdoc (json "sdoc")
        meta (json "meta")]
    (xmldb/replace-doc xmldb (str "/" topic) sdoc)
    (xmldb/run-script
     (xmldb/rf-path "API/topic-get-or-init.xq")
     {:basepath (xmldb/rf-path ".") :xmldb xmldb :topic topic :body body})))

(defn topic
  "Performs different topic operations depending on HTTP method."
  [{:keys [req]
    {:keys [request-method]} :req
    {:keys [path-params]} :match
    {:keys [xmldb]} :host-config :as data}]
  (let [[topic _] (fu/path-to-topic (:key path-params))]
    (case request-method
      :get
      (-> (topic-get-or-default topic data)
          (resp/response)
          (resp/content-type "application/json"))

      :delete
      (-> "<delete/>"
          (resp/response)
          (resp/content-type "application/xml"))

      :put
      (let [body (:body req)
            encoding (or (request/character-encoding req) "UTF-8")
            body-string (slurp body :encoding encoding)]
        (-> (topic-replace topic body-string data)
            (resp/response)
            (resp/content-type "application/xml")))

      (-> (resp/bad-request
           (str "<error>Unknown request: " request-method "</error>"))
          (resp/content-type "application/xml")))))
