(ns rainbowfish.http-app
  (:require [clojure.string :as str]
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
  (if (str/ends-with? path "/")
    (str path "index.topic")
    (str path ".topic")))

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

(defn handler [req]
  (let [host (:server-name req)
        topic (path-to-topic (req/path-info req))]
    (->
     (resp/response (str host topic)#_(render-topic topic))
     (resp/content-type "text/html"))))

(def app
  "Rainbowfish ring application."
  (-> handler
      sess/wrap-session))
