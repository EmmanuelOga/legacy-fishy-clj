(ns rainbowfish.client
  (:require [rainbowfish.dom :as dom]))

(defn open-topic
  [url]
  (let [t (dom/query "#topic-template")
        clone (js/document.importNode (.-content t) true)]
    (dom/get-xml
     (dom/url url :browse true)
     (fn [xml]
       (let [topic (.-outerHTML (dom/query xml "topic"))
             meta (.-textContent (dom/query xml "meta"))
             html (.-innerHTML (dom/query xml "html"))]
         (aset (dom/query clone ".meta textarea") "value" meta)
         (aset (dom/query clone ".topic textarea ") "value" topic)
         (aset (dom/query clone ".preview") "innerHTML" html)
         (.appendChild (dom/query "#main > main") clone))))))

(defonce handlers (atom []))

(defn init
  []
  (let [q (dom/query ".topic-q")
        b (dom/query ".open-q")
        do-open (fn [] (when (.checkValidity q)
                         (open-topic (.-value q))))]
    (dom/on b "click" do-open handlers)
    (dom/on q "keyup"
            (fn [ev] (when (= (.-key ev) "Enter") (do-open)))
            handlers)))

(defn ^:dev/before-load stop
  []
  (dom/cancel-on handlers))

(defn ^:dev/after-load start
  []
  (init))

