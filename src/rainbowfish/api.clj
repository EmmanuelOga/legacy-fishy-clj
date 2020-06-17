(ns rainbowfish.api
  "Request handlers for API methods"
  (:require [ring.util.response :as resp]))

(defn complete-topic
  [req reitit-match]
  (resp/not-found
   (str req " " reitit-match " Coming soon: topic handler")))

(defn topic
  "Performs different topic operations depending on HTTP method."
  [req reitit-match]
  (resp/not-found
   (str req " " reitit-match " Coming soon: topic handler")))
  

