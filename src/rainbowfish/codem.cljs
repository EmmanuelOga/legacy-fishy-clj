(ns rainbowfish.codem
  (:require ["codemirror/keymap/vim"]
            ["codemirror/mode/turtle/turtle"]
            ["codemirror/mode/xml/xml"]
            [codemirror]
            [goog.object :as gob]
            [reagent.core :as rc]
            [reagent.dom :as rd]))

(defn code-mirror
  [value-atom & {:keys [style opts on-init on-change on-save]}]

  (let [cm (atom nil)]
    (rc/create-class
     {:component-did-mount
      (fn [this]
        (let [el (rd/dom-node this)
              options (clj->js
                       (merge
                        {:autoCloseBrackets true
                         :autofocus true
                         :inputStyle "contenteditable"
                         :keyMap "vim"
                         :lineNumbers true
                         :matchBrackets true,
                         :showCursorWhenSelecting true,
                         :value @value-atom
                         :viewportMargin js/Infinity}
                        opts))
              instance (codemirror. el options)]
          (reset! cm instance)
          (.on instance "change"
               (fn [] (let [value (.getValue instance)]
                        (when (not= value @value-atom)
                          (when on-change (on-change value))
                          (reset! value-atom value)))))
          (when on-init (on-init instance))))

      :component-did-update
      (fn [this old-argv]
        (when-not (= @value-atom (.getValue @cm))
          (.setValue @cm @value-atom)
          ;; reset the cursor to the end of the text, if the text was changed externally
          (let [last-line (.lastLine @cm)
                last-ch (count (.getLine @cm last-line))]
            (.setCursor @cm last-line last-ch))))

      :reagent-render
      (fn [_ _ _]
        @value-atom
        [:div {:style style}])})))

