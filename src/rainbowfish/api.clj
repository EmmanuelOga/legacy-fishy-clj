(ns rainbowfish.api
  "Implementation of the API methods."
  (:require [clojure.java.io :as io]
            [rainbowfish.file-util :as fu]
            [rainbowfish.xmldb :as xmldb]
            [ring.util.request :as req]
            [ring.util.response :as resp]))

(defn complete-topic
  "Returns a list of topics that can complete the given query."
  [topic
   {{:keys [request-method]} :req
    {:keys [path-params]} :match
    {:keys [xmldb]} :host-config}]
  (resp/not-found
   (str "WIP: Complete handler.")))

(defn get-topic-or-default
  "Returns a list of topics that can complete the given query."
  [topic
   {{:keys [path-params]} :match
    {:keys [xmldb]} :host-config}]
  (xmldb/run-script
   (xmldb/rf-path "API/topic-get-or-init.xq")
   {:basepath (xmldb/rf-path ".") :xmldb xmldb :topic topic}))

(defn topic
  "Performs different topic operations depending on HTTP method."
  [{{:keys [request-method]} :req
    {:keys [path-params]} :match
    {:keys [xmldb]} :host-config :as data}]
  (let [xml-response (fn [xml]
                       (-> (resp/response xml)
                           (resp/content-type "application/xml")))
        [topic _] (fu/path-to-topic (:key path-params))]
    (case request-method
      :get
      (xml-response (get-topic-or-default topic data))

      :delete
      (xml-response (str "<delete/>"))

      :put
      (xml-response (str "<update>" path-params "</update>"))

      (-> (resp/bad-request
           (str "<error>Unknown request: " request-method "</error>"))
          (resp/content-type "application/xml")))))

