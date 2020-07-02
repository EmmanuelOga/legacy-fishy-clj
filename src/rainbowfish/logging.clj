(ns rainbowfish.logging
  (:require [unilog.config :refer [start-logging!]]))

(defn start
  []
  (start-logging!
   {:console true
    :level "info"
    :files ["./log/app.log"]}))
