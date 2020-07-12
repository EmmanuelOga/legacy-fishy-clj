(ns rainbowfish.client
  (:require [rainbowfish.dom :as dom]
            [rainbowfish.routes :as routes]
            [rainbowfish.codem :as codem]
            [reagent.core :as rc]
            [reagent.dom :as rd]
            [reitit.core :as r]
            [goog.object :as gob]
            [clojure.string :as str]))

(defonce state (rc/atom {;; Key: (str path "-" instance-number)
                         ;; Val: backing data for a topic module.
                         :topics {}
                         ;; Key: topic path. Value: number of topic mods for that path.
                         ;; This is used to generate a unique key for the topic data.
                         :paths-count {}}))

(defn topicmod-add
  [{:keys [path] :as data}]
  (let [{:keys [paths-count topics]} @state
        path-count (inc (paths-count path 0))
        new-key (str path "#" path-count)
        new-data (assoc data :key new-key :path-count path-count)]
    (swap! state assoc
           :topics (assoc topics new-key new-data)
           :paths-count (assoc paths-count path path-count))))

(defn topicmod-update
  [key data]
  (let [topic-data (get-in @state [:topics key])]
    (swap! state assoc-in [:topics key] (merge topic-data data))))

(defn topicmod-close
  [{:keys [key path]}]
  (let [{:keys [topics paths-count]} @state
        new-topics (dissoc topics key)
        new-counts (update paths-count path dec)]
    (swap! state assoc :topics new-topics :paths-count new-counts)))

(defn topicmod-delete
  [{:keys [key path] :as data}]
  (dom/request
   (dom/url (routes/topic-by-name path))
   {:method "DELETE" :headers {:Content-Type "application/json"}}
   (fn [status result]
     (js/console.log status result)
     (if (= status 200)
       (topicmod-close data)
       (js/alert "Error connection to API")))))

(defn topicmod-save
  [{:keys [key path meta sdoc html]}]
  (dom/request
   (dom/url (routes/topic-by-name path))
   {:method "PUT"
    :headers {:Content-Type "application/json"}
    :body (-> (clj->js {:meta meta :sdoc sdoc})
              js/JSON.stringify)}
   (fn [status result]
     (let [sdoc-errors (js->clj (gob/get result "sdoc-errors" #js []))
           meta-errors (js->clj (gob/get result "meta-errors" #js []))]
       (topicmod-update
        key
        (if (= 200 status)
          {:sdoc-errors sdoc-errors
           :meta-errors meta-errors
           :meta (gob/get result "meta")
           :sdoc (gob/get result "sdoc")
           :html (gob/get result "html")}
          {:sdoc-errors sdoc-errors
           :meta-errors meta-errors
           :meta meta
           :sdoc sdoc
           :html html}))))))

(defn error-detail
  [idx {:strs [level message line column]}]
  ^{:key idx}
  [:div.error {:class level}
   [:span.line "Line " line]
   [:span.column "Col " column]
   [:span.message message]])

(defn topicmod
  [{:keys [key path path-count sdoc meta html sdoc-errors meta-errors] :as payload}]
  (fn []
    (let [sdoc-key (str key "-sdoc")
          meta-key (str key "-meta")
          save
          (fn []
            (topicmod-save
             (merge
              payload
              {:meta (codem/read meta-key)
               :sdoc (codem/read sdoc-key)})))]
      [:div.topicmod {:on-key-down
                      (fn [ev]
                        (when (and (= (.-key ev) "s") (.-ctrlKey ev))
                          (.preventDefault ev)
                          (save)))}
       [:div.toolbar
        [:div.name path (if (> path-count 1) (str " #" path-count))]
        [:button.save {:on-click save} "Save"]

        [:button.delete
         {:on-click (fn [_] (topicmod-delete payload))} "Delete"]

        [:button.close
         {:on-click (fn [_] (topicmod-close payload))} "Close"]]

       [:div.content
        [:div.meta
         [:details {:open true}
          [:summary "Meta"]
          [:div.status
           (doall (map-indexed error-detail meta-errors))]
          [codem/create meta-key meta {:mode "text/turtle"}]]]
        [:div.topic
         [:details {:open true}
          [:summary "Source"]
          [:div.status
           (doall (map-indexed error-detail sdoc-errors))]
          [codem/create sdoc-key sdoc {:mode "application/xml"}]]]
        [:div.preview
         [:details {:open true}
          [:summary "Preview"]
          [:div.content (dom/danger html)]]]]])))

(defn render-topicmods
  []
  [:<> (doall
        (map (fn [val] ^{:key (:key val)} [(topicmod val)]) (reverse (vals (:topics @state)))))])

(defn render-topics
  []
  (rd/render
   [render-topicmods]
   (dom/query "#topics-container")))

(defn toolbar-query
  []
  (let [query (rc/atom "")]
    (fn []
      (let [get-valid-query
            (fn [] (let [val @query] (when (not (empty? val)) val)))

            attempt-open-topic
            (fn []
              (when-let [query (get-valid-query)]
                (dom/request
                 (dom/url (routes/topic-by-name query))
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
                  :value @query
                  :on-change (fn [ev] (reset! query (-> ev .-target .-value)))
                  :pattern "[A-Za-z0-9\\/-]*"
                  :on-key-down #(when (= (.-which %) 13) (attempt-open-topic))}]
         [:button.open-q {:on-click #(attempt-open-topic)} "Open"]]))))

(defn render-toolbar
  []
  (rd/render
   [toolbar-query]
   (dom/query "#toolbar-query")))

(defn init
  []
  (render-toolbar)
  (render-topics))

(defn ^:dev/before-load stop
  [])

(defn ^:dev/after-load start
  []
  (init))

