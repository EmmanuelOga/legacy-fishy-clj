(ns rainbowfish.topic
  (:require [rainbowfish.file-util :as fu]))

;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;
;; Retrieve topic parameters from request parameters.
;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;

(def topic-content-types
  {"html" "text/html"
   "topic" "application/xml"
   "ttl" "text/turtle"})

(defn request-to-topic
  "Converts a request path to a [topic-name extension] tuple."
  [{:keys [request-path canonical]}]
  (let [[topic-path topic-basename topic-ext] (fu/get-base-name-and-ext request-path)
        topic-ext (or topic-ext "html")
        topic-name (str (fu/join topic-path (str topic-basename ".topic")))]
    (when-let [content-type (topic-content-types topic-ext)]
      {:topic-name (fu/join "" topic-name)
       :topic-graph (fu/join canonical topic-name)
       :topic-content-type content-type})))
