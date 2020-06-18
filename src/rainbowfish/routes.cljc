(ns rainbowfish.routes
  (:require [rainbowfish.api :as api]
            [reitit.core :as r]))

(def API
  (r/router
   ["/_RF_"
    ["/complete-topic/*key" {:name ::complete-topic
                             :handler #'api/complete-topic}]
    ["/topics/*key" {:name ::topic-by-path
                     :handler #'api/topic}]]))
  

