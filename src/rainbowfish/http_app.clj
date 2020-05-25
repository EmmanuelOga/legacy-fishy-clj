(ns rainbowfish.http-app
  (:require [ring.middleware.session :as sess]
            [rainbowfish.cli :as cli]
            [ring.middleware.file :as ring-file]
            [ring.util.request :as req]
            [ring.util.response :as resp]))

(defn session-test [{session :session}]
  (let [n (session :n 1)]
    (-> (resp/response
         (str "You have visited these many times: " n " times."))
        (assoc-in [:session :n] (inc n)))))

(defn handler [req]
  (let [path (req/path-info req)]
    (condp = path
      "/" (session-test req)
      (resp/not-found "Missing"))))

(def app
  "Rainbowfish ring application."
  (-> handler
      sess/wrap-session
      (ring-file/wrap-file (cli/relpath "static"))))
