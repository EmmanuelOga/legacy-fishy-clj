(ns rainbowfish.public
  "Implementation of the providers for the public content of the sites."
  (:require [clojure.tools.logging :as log]
            [rainbowfish.file-util :as fu]
            [rainbowfish.xmldb :as xmldb]
            [ring.util.request :as req]
            [ring.util.response :as resp]))

(defn topic-get
  [topic content-type {{:keys [xmldb]} :host-config}]
  (xmldb/run-script
   (xmldb/rf-path "public/topic-get.xq")
   {:basepath (xmldb/rf-path ".")
    :xmldb xmldb
    :topic topic
    :content-type content-type}))

(defn get-provider
  "Return a tuple `[provider content-type]` that knows how to return a
  response given a file format (extension)."
  [extension]
  ({"html" [topic-get "text/html"]
    "topic" [topic-get "application/xml"]} extension))

(defn handle-topic
  [{:keys [req] {:keys [xmldb]} :host-config :as data}]
  (if (= :get (:request-method req))
    (let [[basename ext] (fu/path-to-topic (req/path-info req) "html")
          topic (str basename ".topic")]

      (when-let [[provider content-type] (get-provider ext)]
        (let [result
              (provider topic content-type data)

              [{:strs [code] :as opmeta} payload]
              (xmldb/extract-parts result)]

        (log/info "public: GET topic" :basename basename :ext ext :topic topic :result result)

          (->
           (resp/response payload)
           (resp/content-type content-type)))))))
