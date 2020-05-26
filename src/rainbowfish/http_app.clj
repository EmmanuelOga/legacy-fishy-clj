(ns rainbowfish.http-app
  (:require [clojure.string :as str]
            [rainbowfish.cli :as cli]
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
  [path]
  (xmldb/query
   (slurp (rainbowfish.cli/relpath "system/topics.xq"))
   [["$root-path"     (cli/options :root)          "xs:string"]
    ["$database-name" (cli/options :database-name) "xs:string"]
    ["$topic-name"    path                         "xs:string"]
    ["$previewing"    false                        "xs:boolean"]]))

(defn handler [req]
  (if-let [topic (path-to-topic (req/path-info req))]
    (->
     (resp/response (render-topic topic))
     (resp/content-type "application/xml"))
    (resp/not-found "Missing")))

(def app
  "Rainbowfish ring application."
  (-> handler
      sess/wrap-session
      (ring-file/wrap-file (cli/relpath "static"))))
