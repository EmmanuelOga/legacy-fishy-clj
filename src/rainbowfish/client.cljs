(ns rainbowfish.client
  (:require [rainbowfish.dom :as dom]
            [reagent.core :as rc]
            [reagent.dom :as rd]
            [clojure.string :as str]))

(defonce next-id (atom 0))

(defn new-id []
  (swap! next-id inc))

(defonce topic-data (rc/atom (sorted-map)))

(defn add-topic-module
  [data]
  (let [key (new-id)]
    (swap! topic-data assoc key (assoc data :key key))))

(defn remove-topic-module
  [key]
  (swap! topic-data dissoc key))

(defn topic-module
  [{:keys [path topic meta html key]}]
  ^{:key key}
  [:div.topic-module
   [:div.toolbar
    [:div.name path]
    [:button.save "Save"]
    [:button.delete "Delete"]
    [:button.close {:on-click (fn [ev] (remove-topic-module key))} "Close"]]
   [:div.content
    [:div.status]
    [:div.meta
     [:details
      [:summary "Meta"]
      [:textarea {:value meta}]]]
    [:div.topic
     [:details
      [:summary "Source"]
      [:textarea {:value topic}]]]
    [:div.preview
     [:details {:open true}
      [:summary "Preview"]
      [:div.content
       {:dangerouslySetInnerHTML {:__html html}}]]]]])

(defn toolbar-query
  []
  (let [input
        (rc/atom nil)

        get-valid-query
        (fn []
          (let [val (.-value @input)]
            (if (and (.checkValidity @input) (not (empty? val)))
              (if (str/starts-with? val "/") val (str "/" val)))))

        handle-topic-xml
        (fn [path xml]
          (add-topic-module
           {:path path
            :topic (.-outerHTML (dom/query xml "topic"))
            :meta (.-textContent (dom/query xml "meta"))
            :html (.-innerHTML (dom/query xml "html"))}))

        open-topic
        (fn []
          (when-let [query (get-valid-query)]
            (dom/get-xml
             (dom/url query :browse true)
             (fn [xml]
               (if xml
                (handle-topic-xml query xml)
                (js/alert "Error connection to API"))))))]
    [:<>
     [:label {:for "topic-q"} "URL"]
     [:input {:id "topic-q"
              :type "text"
              :ref #(reset! input %)
              :pattern "[A-Za-z0-9\\/-]*"
              :on-key-down #(when (= (.-which %) 13) (open-topic))}]
     [:button.open-q {:on-click #(open-topic)} "Open"]]))

(defn topic-modules
  []
  [:<> (map topic-module (vals @topic-data))])

(defn render-all []
  (rd/render [topic-modules] (dom/query "#topics-container"))
  (rd/render toolbar-query (dom/query "#toolbar-query")))

(defn init
  []
  (render-all))

(defn ^:dev/before-load stop
  [])

(defn ^:dev/after-load start
  []
  (init))

