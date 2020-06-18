(ns rainbowfish.api
  "Request handlers for API methods"
  (:require [ring.util.response :as resp]))

(defn complete-topic
  "Returns a list of topics that can complete the given query."
  [{{:keys [request-method]} :req
    {:keys [path-params]} :match
    {:keys [xmldb]} :host-config}]
  (resp/not-found
   (str req " " reitit-match " Coming soon: topic handler")))

(defn topic
  "Performs different topic operations depending on HTTP method."
  [{{:keys [request-method]} :req
    {:keys [path-params]} :match
    {:keys [xmldb]} :host-config}]
  (case request-method
    :get (resp/not-found (str "Should retrieve " path-params))
    :delete (resp/not-found (str "Should delete " path-params))
    :put (resp/not-found (str "Should save " path-params))
    (resp/bad-request (str "Can't handle " request-method))))
