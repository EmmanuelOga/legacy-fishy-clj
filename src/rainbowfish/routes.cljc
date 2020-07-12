(ns rainbowfish.routes
  (:require [reitit.core :as r]
            #?(:clj [rainbowfish.api :as api])))

(def API
  (r/router
   ["/_RF_"

    #_["/complete-topic/*key"
     {:name ::complete-topic
      #?@(:clj [:handler #'api/topic-complete])}]

    ["/topics/*key"
     {:name ::topic-by-path
      #?@(:clj [:handler #'api/topic])}]]))

(defn topic-by-path
  [query]
  (:path (r/match-by-name API ::topic-by-path {:key query})))


