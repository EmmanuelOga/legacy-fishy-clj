(ns rainbowfish.client
  (:require [rainbowfish.dom :as dom]
            [shadow.remote.runtime.cljs.browser]))

(defn open-topic
  [url]
  (let [t (dom/query "#topic-template")
        clone (js/document.importNode (.-content t) true)]
    (dom/get-xml
     (dom/url (str url ".topic") :browse true)
     (fn [xml]
       (try
         (if-not xml (throw (ex-info "Error" {:msg "Couldn't retrieve Topic"})))
         (let [error (dom/query xml "parsererror")]
           (if error (throw (ex-info "Error" {:msg (.-textContent (.-parentElement error))}))))
         (let [topic (.-outerHTML (dom/query xml "topic"))
               meta (.-textContent (dom/query xml "meta"))
               html (.-innerHTML (dom/query xml "html"))]
           (aset (dom/query clone ".meta textarea") "value" meta)
           (aset (dom/query clone ".topic textarea ") "value" topic)
           (aset (dom/query clone ".preview") "innerHTML" html)
           (.appendChild (dom/query "#main > main") clone))
         (catch js/Error e (js/alert (:msg (ex-data e)))))))))

(defn save-topic
  [dom]
  (let [topic (dom/elem "topic")
        meta (dom/elem "meta")
        root (dom/elem "root")
        fragment (dom/create-fragment)]
    (aset topic "innerText" (.-value (dom/query dom ".topic textarea")))
    (aset meta "innerText" (.-value (dom/query dom ".meta textarea")))
    (.appendChild root topic)
    (.appendChild root meta)
    (.appendChild fragment root)
    (println (dom/serialize-xml fragment))))

(defonce handlers (atom []))

(defn init
  []
  (let [on (fn [n e f] (dom/on n e f handlers))]
    (on
     js/document.body
     "click"
     (fn [ev]
       (let [target (.-target ev)
             topic (dom/get-ancestor target ".topic-module")]
         (when topic
           (cond
             (.matches target ".topic-module-close") (.remove topic)
             (.matches target ".topic-module-save") (save-topic topic))))))

    (let [q (dom/query ".topic-q")
          b (dom/query ".open-q")
          do-open (fn [] (when (.checkValidity q)
                           (open-topic (.-value q))))]
      (on b "click" do-open)
      (on q "keyup" (fn [ev] (when (= (.-key ev) "Enter") (do-open)))))))

(defn ^:dev/before-load stop
  []
  (dom/cancel-on handlers))

(defn ^:dev/after-load start
  []
  (init))

