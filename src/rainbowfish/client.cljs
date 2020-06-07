(ns rainbowfish.client
  (:require [rainbowfish.dom :as dom]))

(defn open-topic
  [url]
  (let [t (dom/query "#topic-template")
        clone (js/document.importNode (.-content t) true)
        c (dom/query "#main>main")]
    (.appendChild c clone)))

(defn init
  []
  (.log js/console "Ready.")
  (open-topic "/"))

(defn ^:dev/after-load start []
  (js/console.log "Start")
  (open-topic "/"))
  

(defn ^:dev/before-load stop []
  (js/console.log "Stop"))
