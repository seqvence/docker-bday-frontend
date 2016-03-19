(ns docker-bday-frontend.core_map
  (:require-macros [cljs.core.async.macros :refer [go go-loop]])
  (:require [cljs.core.async :refer [>! <! chan put!]]
            [docker-bday-frontend.map :as dmap]
            [docker-bday-frontend.chart :as dchart]
            [docker-bday-frontend.chart_strokes :as schart]
            [docker-bday-frontend.components :as components]
            [reagent.core :as reagent]
            [reagent.session :as session]
            [goog.events :as events]
            [goog.history.EventType :as EventType]
            [secretary.core :as secretary :include-macros true]
            [ajax.core :refer [GET POST]]
            [gmaps.components.reagent.map :as rmap]
            [rigui.core :refer [start later! every! stop cancel!]]
            [cljsjs.react-bootstrap])
  (:import goog.History))

(enable-console-print!)

(defonce app-state (reagent/atom {:show-modal false :stats {"submissions" [] "votes" {:languages []}} :instructions "Instructions content"}))

;;-------------------------
;; Backend comm.

(defn response-handler [response]
  ;(println (str "Received response from backend: " response))
  (swap! app-state assoc-in [:stats "submissions"] (get response "submissions"))
  (swap! app-state assoc-in [:stats "votes" :languages] (into [] (for [[k v] (get response "votes")] {:label k :votes v})))
  (doseq [submission (get response "submissions")]
    (dmap/add-marker submission)))

(defn error-handler [{:keys [status status-text]}]
  (println (str "something bad happened: " status " " status-text)))


(defn get-stats []
  (GET "/stats"
       {:handler response-handler
        :error-handler error-handler
        :format :json
        :response-format :json}))

;;--------------------------
;; Pages

(defn home-page []
  [:div

    [:div {:id "map-container" :style {:position "relative" :width "100%" :height "100%" :padding-bottom "65%" :border-width "1px"
                                       :border-style "solid" :border-color "#ccc #ccc #999 #ccc"
                                       :box-shadow "rgba(64, 64, 64, 0.1) 0 2px 5px"
                                       :-webkit-box-shadow "rgba(64, 64, 64, 0.5) 0 2px 5px"
                                       :-moz-box-shadow "rgba(64, 64, 64, 0.5) 0 2px 5px"}}
      [rmap/map-view @dmap/map-data]]])


(defn current-page []
  [:div [(session/get :current-page)]])

;;-------------------------
;; Routes

(secretary/set-config! :prefix "#")

(secretary/defroute "/" []
                    (session/put! :current-page home-page))

;; -------------------------
;; History

(defn hook-browser-navigation! []
  (doto (History.)
    (events/listen
      EventType/NAVIGATE
      (fn [event]
        (secretary/dispatch! (.-token event))))
    (.setEnabled true)))

;; -------------------------
;; Initialize app

(def clustering_styles  [{:url "/cluster_docker.png"
                          :anchor [0 14]
                          :height 31
                          :width 48}
                         {:url "/cluster_docker.png"
                          :anchor [0 12]
                          :height 31
                          :width 48}
                         {:url "/cluster_docker.png"
                          :anchor [0 10]
                          :height 31
                          :width 48}])

(defn mount-root []
  (reagent/render [current-page] (.getElementById js/document "app")))

(defn init! []
  (swap! dmap/map-data assoc :clustering_styles clustering_styles)
  (hook-browser-navigation!)
  (mount-root)
  (get-stats))

(init!)