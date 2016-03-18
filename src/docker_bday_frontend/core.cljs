(ns docker-bday-frontend.core
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

(defn instructions-handler [response]
  (swap! app-state assoc :instructions response))

(defn error-handler [{:keys [status status-text]}]
  (println (str "something bad happened: " status " " status-text)))

(defn submission-handler [response]
  (println "Received submission information")
  (swap! app-state assoc :submission-info response))

(defn get-submission [id submission-handler]
  (println "Getting info for " id)
  (GET (str "/competition/" id)
       {:handler submission-handler
        :error-handler error-handler
        :format :json
        :response-format :json}))

(defn get-stats []
  (GET "/stats"
       {:handler response-handler
        :error-handler error-handler
        :format :json
        :response-format :json}))

(defn get-instructions []
  (GET "/tutorial_outline.html"
       {:handler instructions-handler
        :error-handler error-handler}))

;;--------------------------
;; Pages

(def Button (reagent/adapt-react-class (aget js/ReactBootstrap "Button")))

(defn home-page []
  [:div
    [components/header]
    [:a {:href "https://twitter.com/intent/tweet?text=Just%20completed%20the%20%23dockerbday%20training!%20Built%2C%20shipped%20%26%20ran%20my%20first%20app%20using%20%23docker&via=docker&url=http%3A%2F%2Fdocker.party"
         :target "_blank"}

     [:img {:src "/dockerbday_tweet_button3.png" :class "img-responsive center-block" :style {:width "60%" :height "60%"}}]]
    [components/submission-modal get-submission app-state]
    [:div {:id "map-container" :style {:position "relative" :width "100%" :padding-bottom "65%" :border-width "1px"
                                       :border-style "solid" :border-color "#ccc #ccc #999 #ccc"
                                       :box-shadow "rgba(64, 64, 64, 0.1) 0 2px 5px"
                                       :-webkit-box-shadow "rgba(64, 64, 64, 0.5) 0 2px 5px"
                                       :-moz-box-shadow "rgba(64, 64, 64, 0.5) 0 2px 5px"}}
      [rmap/map-view @dmap/map-data]]
    [:div {:id "chart-container" }
      [:h4 {:class "text-muted"} "Programming language usage"]
      [schart/d3-inner  (get-in @app-state [:stats "votes"])]]])


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


(def stats-timer (start 10 8 get-stats))


(defn mount-root []
  (reagent/render [current-page] (.getElementById js/document "app")))

(defn init! []
  (hook-browser-navigation!)
  (mount-root)
  (get-stats)
  (every! stats-timer "test"
          0;; initial delay
          3000 ;; interval
          )
  (get-instructions))

(init!)