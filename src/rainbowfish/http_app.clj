(ns rainbowfish.http-app
  (:require [clojure.java.io :as io]
            [reitit.core :as r]
            [rainbowfish.routes :as routes]
            [rainbowfish.config :as config]
            [rainbowfish.file-util :as fu]
            [rainbowfish.xmldb :as xmldb]
            [ring.middleware.file :as ring-file]
            [ring.middleware.params :as params]
            [ring.middleware.session :as sess]
            [ring.util.request :as req]
            [ring.util.response :as resp]))

(defn session-test [{session :session}]
  (let [n (session :n 1)]
    (-> (resp/response
         (str "You have visited these many times: " n " times."))
        (assoc-in [:session :n] (inc n)))))

(defn path-to-topic
  "Converts a request path to a topic."
  [path]
  (let [[base name ext] (fu/get-base-name-and-ext path)]
    [(str (or base "/") (or name "index")) (or ext "html")]))

(defn render-topic
  "Renders a topic given a path.
  Dispatches to BaseX, which should find and render the topic."
  [{:keys [host assets-path xmldb topic format params] :as info}]
  (xmldb/query
   (slurp (io/resource "assets/xquery/topics.xq"))
   [["$assets-path" assets-path]
    ["$browse" (= "true" (params "browse")) "xs:boolean"]
    ["$format" format]
    ["$host" host]
    ["$topic" topic]
    ["$xmldb" xmldb]
    ["$default-triples" (slurp (io/resource "assets/triples/default.ttl"))]
    ["$default-topic" (slurp (io/resource "assets/xml/default.topic"))]
    ["$xsl-topic" (slurp (io/resource "assets/xsl/topic.xsl"))]]))

(defn get-provider
  "Return a tuple `[provider content-type]` that knows how to return a
  response given a file format (extension)."
  [extension]
  ({"html" [render-topic "text/html"]
    "topic" [render-topic "application/xml"]} extension))

(defn handle-public
  "Inner method of the HTTP handler. At this point we know the host
  requested exists, and we have the XMLDB name."
  [req host assets-path xmldb]
  (let [[path-name path-format] (path-to-topic (req/path-info req))]
    (when-let [[provider content-type] (get-provider path-format)]
      (let [info {:host host
                  :assets-path assets-path
                  :xmldb xmldb
                  :topic path-name
                  :format path-format
                  :params (:params req)}]
        (->
         (resp/response (provider info))
         (resp/content-type content-type))))))

(defn handler
  "Rainbowfish's main HTTP request handler."
  [req]
  ; Host: support proxying of requests. Typically the request will
  ; come from the server that host the static content, like nginx or
  ; shadow-cljs during development.
  (let [host (or (get-in req [:headers "x-forwarded-server"])
                 (:server-name req))]

    (or
     ; Check if the host is known (must be one of the configured sites).
     (when-let [{:keys [assets-path xmldb]} (get-in (config/config) [:hosts host])]

       (or
        ; Check if we are handling an API call.
        (when-let [match (r/match-by-path routes/API (req/path-info req))]
          ((:result match) req match))

        ; Check if there's a static file first.
        (ring-file/file-request req (str assets-path "/static"))

        ; Otherwise check if we can dynamically generate content.
        (handle-public req host assets-path xmldb)))

        ; Finally... admit defeat :-).
     (resp/not-found "Resource not found."))))

(defn app
  "Rainbowfish ring application."
  []
  (-> handler
      params/wrap-params
      sess/wrap-session))
