(ns rainbowfish.public
  "Implementation of the providers for the public content of the sites."
  (:require [clojure.java.io :as io]
            [rainbowfish.file-util :as fu]
            [rainbowfish.xmldb :as xmldb]
            [ring.util.request :as req]
            [ring.util.response :as resp]))

(defn get-topic-as-html
  "Renders a topic as html given its request parameters.
   May render a 404."
  [topic {{:keys [xmldb]} :host-config}]
  (xmldb/query
   (slurp (io/resource "assets/public/topic-as-html.xq"))
   [["$xmldb" xmldb]
    ["$topic" topic]
    ["$xsl-topic" (slurp (io/resource "assets/public/topic-to-html.xsl"))]]))

(defn get-topic-as-xml
  "Renders a topic as html given its request parameters.
   May render a 404."
  [topic {{:keys [xmldb]} :host-config}]
  "TODO")

(defn get-provider
  "Return a tuple `[provider content-type]` that knows how to return a
  response given a file format (extension)."
  [extension]
  ({"html" [get-topic-as-html "text/html"]
    "topic" [get-topic-as-xml "application/xml"]} extension))

(defn handle-topic
  [{:keys [req] {:keys [xmldb]} :host-config :as data}]
  (if (= :get (:request-method req))
    (let [[topic format] (fu/path-to-topic (req/path-info req))]
      (when-let [[provider content-type] (get-provider format)]
        (if (= "true"
               (xmldb/fire
                (str "SET BINDINGS $db=" xmldb ", $path=" "index")
                (str "XQUERY declare variable $db as xs:string external;
                            declare variable $path as xs:string external;
                            db:exists($db, $path)")))
          (->
           (resp/response (provider topic data))
           (resp/content-type content-type)))))))
