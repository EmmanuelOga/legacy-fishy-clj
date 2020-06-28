(ns rainbowfish.client
  (:require [rainbowfish.dom :as dom]
            [rainbowfish.routes :as routes]
            [reagent.core :as rc]
            [reagent.dom :as rd]
            [reitit.core :as r]
            [clojure.string :as str]))

(defonce next-id (atom 0))

(defn new-id []
  (swap! next-id inc))

(defonce topic-data (rc/atom (sorted-map)))

(defn topicmod-add
  [data]
  (let [key (new-id)]
    (swap! topic-data assoc key (assoc data :key key))))

(defn topicmod-update
  [{:keys [key] :as data}]
  (swap! topic-data assoc key data))

(defn topicmod-close
  [key]
  (swap! topic-data dissoc key))

(defn topicmod-delete
  [key]
  (js/alert (str "TODO: request delete of " key)))

(defn topicmod-save
  [{:keys [key path meta sdoc]}]
  (dom/request
   (dom/url (routes/topic-by-path path))
   {:method "PUT"
    :headers {:Content-Type "application/json"}
    :body (-> (clj->js {:meta meta :sdoc sdoc})
              js/JSON.stringify)}
   (fn [result]
     (if result
       (topicmod-update
        {:key key
         :path path
         :sdoc (goog.object/get result "topic")
         :meta (goog.object/get result "meta")
         :html (goog.object/get result "html")})
       (js/alert "Error saving the topic.")))))

(defn topicmod
  [{:keys [key path sdoc meta html]}]
  (let [ta-sdoc (rc/atom nil) ta-meta (rc/atom nil)]
    ^{:key key}
    [:div.topicmod
     [:div.toolbar
      [:div.name path]
      [:button.save   {:on-click (fn [_] (topicmod-save
                                          {:key key
                                           :path path
                                           :meta (.-value @ta-meta)
                                           :sdoc (.-value @ta-sdoc)}))} "Save"]
      [:button.delete {:on-click (fn [_] (topicmod-delete key))} "Delete"]
      [:button.close  {:on-click (fn [_] (topicmod-close key))} "Close"]]
     [:div.content
      [:div.status]
      [:div.meta
       [:details
        [:summary "Meta"]
        [:textarea {:defaultValue meta
                    :ref #(reset! ta-meta %)}]]]
      [:div.topic
       [:details
        [:summary "Source"]
        [:textarea {:defaultValue sdoc
                    :ref #(reset! ta-sdoc %)}]]]
      [:div.preview
       [:details {:open true}
        [:summary "Preview"]
        [:div.content (dom/danger html)]]]]]))

(defn toolbar-query
  []
  (let [input (rc/atom nil)

        get-valid-query
        (fn []
          (let [val (.-value @input)]
            (if (and (.checkValidity @input) (not (empty? val)))
              (if (str/starts-with? val "/") val (str "/" val)))))

        attempt-open-topic
        (fn []
          (when-let [query (get-valid-query)]
            (dom/request
             (dom/url (routes/topic-by-path query))
             {:method "GET" :headers {:Content-Type "application/json"}}
             (fn [result]
               (if result
                 (topicmod-add
                  {:path query
                   :meta (goog.object/get result "meta")
                   :sdoc (goog.object/get result "sdoc")
                   :html (goog.object/get result "html")})
                 (js/alert "Error connection to API"))))))]
    [:<>
     [:label {:for "topic-q"} "URL"]
     [:input {:id "topic-q"
              :type "text"
              :ref #(reset! input %)
              :pattern "[A-Za-z0-9\\/-]*"
              :on-key-down #(when (= (.-which %) 13) (attempt-open-topic))}]
     [:button.open-q {:on-click #(attempt-open-topic)} "Open"]]))

(defn topicmods
  []
  [:<> (map topicmod (vals @topic-data))])

(defn render-all []
  (rd/render [topicmods] (dom/query "#topics-container"))
  (rd/render toolbar-query (dom/query "#toolbar-query")))

(defn init
  []
  (render-all))

(defn ^:dev/before-load stop
  [])

(defn ^:dev/after-load start
  []
  (init))

