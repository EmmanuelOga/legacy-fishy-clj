(ns rainbowfish.http-app
  (:require [rainbowfish.config :as config]
            [rainbowfish.public :as public]
            [rainbowfish.routes :as routes]
            [reitit.core :as r]
            [ring.middleware.file :as ring-file]
            [ring.middleware.params :as params]
            [ring.middleware.session :as sess]
            [ring.util.request :as req]
            [ring.util.response :as resp]))

(defn session-test [{session :session}]
  (let [n (session :n 1)]
    (-> (resp/response
         (str "You have visited these many times: " n " times."))
        (assoc-in [:session :n] (inc n)))))

(defn handler
  "The host on the request should be set and match against one of the
  site hosts on the configuration."
  [req]
  (let [host (or (get-in req [:headers "x-forwarded-server"])
                 (:server-name req))]

    (or
     (when-let [{:keys [assets-path] :as host-config}
                (get-in (config/config) [:hosts host])]

       (or
        (when-let [match (r/match-by-path routes/API (req/path-info req))]
          (@(:result match) {:req req :match match :host-config host-config}))

        (ring-file/file-request req (str assets-path "/static"))

        (@#'public/handle-topic {:req req :host-config host-config})))

     (resp/not-found "Resource not found."))))

(defn app
  "Rainbowfish ring application."
  []
  (-> handler
      params/wrap-params
      sess/wrap-session))
