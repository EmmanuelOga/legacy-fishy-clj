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
  [{:keys [request-path assets-path] :as request}]
  (or
   (when-let [match (r/match-by-path routes/API request-path)]
     (@(:result match) (assoc request :route-match match)))
   (@#'public/handle-topic request)
   (resp/not-found "Resource not found.")))

(defn find-host-config
  [handler]
  (fn
    ([{:keys [request-method server-name headers] :as request}]
     (let [request-host (or (headers  "x-forwarded-server") server-name)
           {:keys [hosts rdf-server]} (config/config)
           host-config (hosts request-host)]
       (or
        (when-not host-config (resp/not-found "Resource not found."))
        (ring-file/file-request request (str (:assets-path host-config) "/static"))
        (handler (merge request
                        host-config
                        {:request-host request-host
                         :request-path (req/path-info request)
                         :rdf-server rdf-server})))))
    ([request respond raise]
     (handler respond raise))))

(defn app
  "Rainbowfish ring application."
  []
  (-> handler
      find-host-config
      params/wrap-params
      sess/wrap-session))
