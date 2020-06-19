(ns rainbowfish.routes
  (:require [reitit.core :as r]
            #?(:clj [rainbowfish.api :as api])))

(def API
  (r/router
   ["/_RF_"

    ["/complete-topic/*key"
     {:name ::complete-topic
      #?@(:clj [:handler #'api/complete-topic])}]

    ["/topics/*key"
     {:name ::topic-by-path
      #?@(:clj [:handler #'api/topic])}]]))
