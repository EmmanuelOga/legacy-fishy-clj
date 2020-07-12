(ns rainbowfish.routes
  (:require [reitit.core :as r]
            #?(:clj [rainbowfish.api :as api])))

(def API
  (r/router
   ["/_RF_"

    #_["/complete-topic/*topic-name"
     {:name ::complete-topic
      #?@(:clj [:handler #'api/topic-complete])}]

    ["/topics/*topic-name"
     {:name ::topic-by-name
      #?@(:clj [:handler #'api/topic])}]]))

(defn topic-by-name
  [topic-name]
  (:path (r/match-by-name API ::topic-by-name {:topic-name topic-name})))


