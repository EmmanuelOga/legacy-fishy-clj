(ns rainbowfish.http-app
  (:require [clojure.string :as str]
            [rainbowfish.config :as config]
            [rainbowfish.file-util :as fu]
            [rainbowfish.xmldb :as xmldb]
            [ring.middleware.file :as ring-file]
            [ring.middleware.session :as sess]
            [ring.middleware.params :as params]
            [ring.util.request :as req]
            [ring.util.response :as resp]
            [clojure.java.io :as io]))

(defn session-test [{session :session}]
  (let [n (session :n 1)]
    (-> (resp/response
         (str "You have visited these many times: " n " times."))
        (assoc-in [:session :n] (inc n)))))

(defn path-to-topic
  "Converts a request path to a topic"
  [path]
  (let [[name ext] (fu/get-base-and-ext path)]
    [(if name name "index") (if ext ext "html")]))

(defn render-topic
  "Renders a topic given a path.
  Dispatches to BaseX, which should find and render the topic."
  [{:keys [host assets-path xmldb topic format params] :as info}]
  (let [is-browse (= "true" (params "browse"))
        xsl (slurp (io/resource "assets/xsl/topic.xsl"))
        query (slurp (io/resource "assets/xquery/topics.xq"))]
    (xmldb/query
     query
     [["$assets-path" assets-path "xs:string"]
      ["$browse"      is-browse   "xs:boolean"]
      ["$format"      format      "xs:string"]
      ["$host"        host        "xs:string"]
      ["$topic"       topic       "xs:string"]
      ["$xmldb"       xmldb       "xs:string"]
      ["$xsl"         xsl         "xs:string"]])))

(defn get-provider
  "Return a tuple `[provider content-type]` that knows how to return a
  response given a file format (extension)."
  [extension]
  ({"html" [render-topic "text/html"]} extension))

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
        ; Check if there's a static file first.
        (ring-file/file-request req (str assets-path "/static"))

        ; Otherwise check if we can dynamically generate content.
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
               (resp/content-type content-type)))))))

        ; Finally... admit defeat :-).
     (resp/not-found "Resource not found."))))

(defn app
  "Rainbowfish ring application."
  []
  (-> handler
      params/wrap-params
      sess/wrap-session))
