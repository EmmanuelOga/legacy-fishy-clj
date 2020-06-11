(ns rainbowfish.client
  (:require [rainbowfish.dom :as dom]
            [reagent.core :as r]
            [shadow.remote.runtime.cljs.browser]))

(defn simple-component
  []
  [:div.topic-module
   [:div.topic-toolbar
    [:div.name]
    [:button.topic-module-save "Save"]
    [:button.topic-module-delete "Delete"]
    [:button.topic-module-close "Close"]]
   [:div.topic-content
    [:div.status]
    [:div.meta
     [:details
      [:summary "Meta"]
      [:textarea]]]
    [:div.topic
     [:details {"open" "open"}
      [:summary "Source"]
      [:textarea]]]
    [:div.preview]]])

(defn render-simple []
  (r/render
   [simple-component]
   (dom/query "#topics-container")))

(defn init
  []
  (render-simple))

(defn ^:dev/before-load stop
  [])

(defn ^:dev/after-load start
  []
  (init))

