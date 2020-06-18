(ns rainbowfish.public
  (:require [clojure.java.io :as io]
            [rainbowfish.config :as config]
            [rainbowfish.file-util :as fu]
            [rainbowfish.routes :as routes]
            [rainbowfish.xmldb :as xmldb]
            [reitit.core :as r]
            [ring.middleware.file :as ring-file]
            [ring.middleware.params :as params]
            [ring.middleware.session :as sess]
            [ring.util.request :as req]
            [ring.util.response :as resp]))

(defn get-topic-as-html
  "Renders a topic as html given its request parameters.
   May render a 404."
  (xmldb/query
   (slurp (io/resource "assets/xquery/public/topics.xq"))
   [["$xmldb" xmldb]
    ["$topic" topic]
    ["$xsl-topic" (slurp (io/resource "assets/xsl/topic.xsl"))]]))

(defn get-topic-as-xml
  "Renders a topic as html given its request parameters.
   May render a 404."
  (xmldb/query
   (slurp (io/resource "assets/xquery/public/topics.xq"))
   [["$xmldb" xmldb]
    ["$topic" topic]
    ["$xsl-topic" (slurp (io/resource "assets/xsl/topic.xsl"))]]))

(defn get-provider
  "Return a tuple `[provider content-type]` that knows how to return a
  response given a file format (extension)."
  [extension]
  ({"html" [render-topic "text/html"]
    "topic" [render-topic "application/xml"]} extension))

(defn path-to-topic
  "Converts a request path to a topic."
  [path]
  (let [[base name ext] (fu/get-base-name-and-ext path)]
    [(str (or base "/") (or name "index")) (or ext "html")]))

(defn handle-topic
  [{{:keys [request-method]} :req
    {:keys [path-params]} :match
    {:keys [xmldb]} :host-config}]
  (let [[path-name path-format] (path-to-topic path-params)]
    (when-let [[provider content-type] (get-provider path-format)]
        (->
         (resp/response (provider info))
         (resp/content-type content-type))))))
