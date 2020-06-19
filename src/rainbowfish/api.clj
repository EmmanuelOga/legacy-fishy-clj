(ns rainbowfish.api
  "Request handlers for API methods"
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
  (xmldb/query
   (slurp (io/resource "assets/API/topic-get-or-init.xq"))
   [["$xmldb" xmldb]
    ["$topic" topic]
    ["$xsl-topic" (slurp (io/resource "assets/API/topic-to-html-snippet.xsl"))]
    ["$default-topic" (slurp (io/resource "assets/API/default.topic"))]
    ["$default-triples" (slurp (io/resource "assets/API/default.ttl"))]]))

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
      (xml-response (str "<update/>"))

      (-> (resp/bad-request
           (str "<error>Unknown request: " request-method "</error>"))
          (resp/content-type "application/xml")))))

