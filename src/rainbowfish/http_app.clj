(ns rainbowfish.http-app
  (:require [clojure.string :as str]
            [rainbowfish.config :as config]
            [rainbowfish.file-util :as fu]
            [rainbowfish.xmldb :as xmldb]
            [ring.middleware.file :as ring-file]
            [ring.middleware.session :as sess]
            [ring.util.request :as req]
            [ring.util.response :as resp]))

(defn session-test [{session :session}]
  (let [n (session :n 1)]
    (-> (resp/response
         (str "You have visited these many times: " n " times."))
        (assoc-in [:session :n] (inc n)))))

(defn path-to-topic
  "Converts a request path to a topic"
  [path]
  (let [[name ext] (fu/get-base-and-ext path)]
    (if-not ext [name "html"] [name ext])))

(defn render-topic
  "Renders a topic given a path.
  Dispatches to BaseX, which should find and render the topic."
  [site-path db-name topic-path]
  (xmldb/query
   #_(slurp (rainbowfish.cli/relpath "system/topics.xq"))
   ""
   [["$root-path"     site-path  "xs:string"]
    ["$database-name" db-name    "xs:string"]
    ["$topic-name"    topic-path "xs:string"]
    ["$previewing"    false      "xs:boolean"]]))

(defn handler
  "Rainbowfish's main HTTP request handler."
  [req]
  (let [host (or (get-in req [:headers "x-forwarded-server"])
                 (:server-name req))]
    (if-let [{:keys [assets-path xmldb]} (get-in (config/config) [:hosts host])]
      (let [[topic-name topic-format] (path-to-topic (req/path-info req))
            info {:host host
                  :assets-path assets-path
                  :xmldb xmldb
                  :topic topic-name
                  :format topic-format}]
        (or (ring-file/file-request req assets-path)
            (->
             (resp/response (str info))
             (resp/content-type "text/html"))))
      (resp/not-found (str "Resource not found: " host req)))))

(defn app
  "Rainbowfish ring application."
  []
  (-> handler
      sess/wrap-session))
