(ns rainbowfish.client
  (:require [rainbowfish.dom :as dom]
            [rainbowfish.routes :as routes]
            [reagent.core :as rc]
            [reagent.dom :as rd]
            [reitit.core :as r]
            [goog.object :as gob]
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

(defn topicmod-partial-update
  [{:keys [key] :as data}]
  (swap!
   topic-data
   (fn [prev-data]
     (assoc prev-data key (merge (prev-data key) data)))))

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
   (fn [status result]
     (let [sdoc-errors (js->clj (gob/get result "sdoc-errors" #js []))]
       (if (= 200 status)
         (topicmod-update
          {:key key
           :path path
           :sdoc-errors sdoc-errors
           :meta (gob/get result "meta")
           :sdoc (gob/get result "sdoc")
           :html (gob/get result "html")})
         (topicmod-partial-update
          {:key key :sdoc-errors sdoc-errors}))))))

(defn error-detail
  [idx {:strs [level message line column]}]
  ^{:key idx}
  [:div.error {:class level}
   [:span.line "Line " line]
   [:span.column "Col " column]
   [:span.message message]])

(defn topicmod
  [{:keys [key path sdoc meta html sdoc-errors]}]
  (let [ta-sdoc (rc/atom nil) ta-meta (rc/atom nil)]
    ^{:key key}
    [:div.topicmod
     [:div.toolbar
      [:div.name path]
      [:button.save
       {:on-click
        (fn [_]
          (topicmod-save
           {:key key
            :path path
            :meta (.-value @ta-meta)
            :sdoc (.-value @ta-sdoc)}))} "Save"]

      [:button.delete
       {:on-click
        (fn [_] (topicmod-delete key))} "Delete"]

      [:button.close
       {:on-click
        (fn [_] (topicmod-close key))} "Close"]]
     [:div.content
      [:div.meta
       [:details
        [:summary "Meta"]
        [:textarea {:defaultValue meta
                    :ref #(reset! ta-meta %)}]]]
      [:div.topic
       [:details {:open true}
        [:summary "Source"]
        [:div.status
         (map-indexed error-detail sdoc-errors)]
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
             (fn [status result]
               (if (= status 200)
                 (topicmod-add
                  {:path query
                   :meta (gob/get result "meta")
                   :sdoc (gob/get result "sdoc")
                   :html (gob/get result "html")})
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

