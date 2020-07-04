(ns rainbowfish.codem
  (:require ["codemirror/keymap/vim"]
            ["codemirror/mode/turtle/turtle"]
            ["codemirror/mode/xml/xml"]
            [codemirror]
            [goog.object :as gob]
            [reagent.core :as rc]
            [reagent.dom :as rd]))

(defonce instances (atom {}))

(defn read
  [key]
  (.getValue (@instances key)))

(defn create
  [key val opts]
  (rc/create-class
   {:component-did-mount
    (fn [this]
      (let [options (clj->js
                     (merge
                      {:autoCloseBrackets true
                       :inputStyle "contenteditable"
                       :keyMap "vim"
                       :lineNumbers true
                       :matchBrackets true,
                       :showCursorWhenSelecting true,
                       :value val
                       :viewportMargin js/Infinity}
                      opts))]
        (swap! instances assoc key (codemirror. (rd/dom-node this) options))))

    :component-did-update
    (fn [this]
      (when-let [instance (@instances key)]
        (.setValue (@instances key) val)))

    :component-will-unmount
    (fn [this]
      (swap! instances dissoc key))

    :reagent-render
    (fn [_ _ _] [:div.codem])}))

