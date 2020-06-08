(ns rainbowfish.client
  (:require [rainbowfish.dom :as dom]))

(defn open-topic
  [url]
  (let [t (dom/query "#topic-template")
        clone (js/document.importNode (.-content t) true)
        c (dom/query "#main>main")]
    (.appendChild c clone)))

(defonce handlers (atom []))

(defn init
  []
  (dom/on (dom/query ".open-topic")
          "keyup"
          (fn [ev]
            (let [target (.-target ev)]
              (when (and (= (.-key ev) "Enter")
                         (.checkValidity target))
                (open-topic (.-value target)))))
          handlers))

(defn ^:dev/before-load stop
  []
  (dom/cancel-on handlers))

(defn ^:dev/after-load start
  []
  (init))

