(ns rainbowfish.dom
  (:require [clojure.string :as str]))

(defn get-ancestor
  [node selector]
  (loop [elem node]
    (when elem
      (if (.matches elem selector)
        elem
        (recur (.-parentElement elem))))))

(defn on
  [elem event-name handler & [storage]]
  (when elem
    (.addEventListener elem event-name handler)
    (let [uninst (fn [] (.removeEventListener elem event-name handler))]
      (when storage (swap! storage conj uninst))
      uninst)))

(defn cancel-on
  [storage]
  (swap!
   storage
   (fn [old-handlers]
     (run! (fn [uninst] (uninst)) old-handlers)
     [])))

(defn matches
  [target selector]
  (and target (.matches target selector) target))

(defn query
  ([selector]
   (query js/document selector))
  ([elem selector]
   (and
    elem
    (.querySelector elem selector))))

(defn append
  ([elem html]
   (append elem html "beforeend"))
  ([elem html position]
   (when (and elem html)
     (.insertAdjacentHTML elem position html))))

(defn insert-before
  [elem insertee]
  (.insertBefore (.-parentElement elem) insertee elem))

(defn inner-html
  [elem]
  (when elem (.-innerHTML elem)))

(defn children
  [elem]
  (when elem (.-childNodes elem)))

(defn attr
  ([elem key]
   (.getAttribute elem key))
  ([elem key val]
   (.setAttribute elem key (clj->js val))
   (.getAttribute elem key)))

(defn data
  ([elem key]
   (attr elem (str "data-" key)))
  ([elem key val]
   (attr elem (str "data-" key) val)))

(defn add-class
  [elem val]
  (.add (.-classList elem) val))

(defn remove-class
  [elem val]
  (.remove (.-classList elem) val))

(defn create-fragment
  []
  (js/document.createDocumentFragment))

(defn elem
  [elem-name & params]
  (let [elem (.createElement js/document (name elem-name))
        setter (fn [[key val]]
                 (if (= key :class)
                   (add-class elem val)
                   (attr elem key val)))]
    (run! setter (seq params))
    elem))

(defn current-host
  []
  (let [loc js/document.location]
    (str (.-protocol loc) "//" (.-host loc))))

(defn url
  [path & {:as params}]
  (let [url (js/URL. (str (current-host) path))
        url-params (.-searchParams url)
        set-param (fn [[key val]]
                    (.set url-params (clj->js key) (clj->js val)))]
    (run! set-param (seq params))
    url))

(defn request
  [url options callback]
  (.then (js/fetch (js/Request. url) (clj->js options))
         (fn [res] (.then (.text res)
                          (fn [text]
                            (callback
                             (.-status res)
                             (try (js/JSON.parse text))))))))

(defn danger
  "This is react way to escape inserting inner html"
  [html]
  {:dangerouslySetInnerHTML {:__html html}})
