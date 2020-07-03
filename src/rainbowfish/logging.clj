(ns rainbowfish.logging
  (:require [unilog.config :refer [start-logging!]]))

(defn start
  []
  (start-logging!
   {:console false
    :level "info"
    :files ["./log/app.log"]}))
