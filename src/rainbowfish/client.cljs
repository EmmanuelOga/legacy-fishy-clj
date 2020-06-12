(ns rainbowfish.client
  (:require [rainbowfish.dom :as dom]
            [reagent.core :as rc]
            [reagent.dom :as rd]
            [shadow.remote.runtime.cljs.browser]))

(defonce topic-data (rc/atom []))

(defn topic-module
  [data]
  ^{:key data}[:div.topic-module
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
     [:details
      [:summary "Source"]
      [:textarea]]]
    [:div.preview
     [:details {:open true}
      [:summary "Preview"]
      [:div.content "Loading..."]]]]])

(defn topic-modules
  []
  [:<> (map topic-module @topic-data)])

(defn render-all []
  (rd/render
   [topic-modules]
   (dom/query "#topics-container")))

(defn init
  []
  (render-all))

(defn ^:dev/before-load stop
  [])

(defn ^:dev/after-load start
  []
  (init))

