(ns docker-bday-frontend.core
  (:require [reagent.core :as reagent]
            [reagent.session :as session]
            [goog.events :as events]
            [goog.history.EventType :as EventType]
            [secretary.core :as secretary :include-macros true]
            [ajax.core :refer [GET POST]])
  (:import goog.History))

(enable-console-print!)

(defonce app-state (atom {:stats "Initial state"}))

;;-------------------------
;; Backend comm.

(defn response-handler [response]
  (println (str "Received response from backend: " response))
  (swap! app-state assoc :stats response))

(defn error-handler [{:keys [status status-text]}]
  (println (str "something bad happened: " status " " status-text)))

(defn get-stats []
  (println (str "Retrieving stats"))
  (GET "/stats"
       {:handler response-handler
        :error-handler error-handler
        :format :json
        :response-format :json}))

;;--------------------------
;; Pages

(defn home-page []
  (get-stats)
  [:div
    [:button {:on-click (fn [e] (.preventDefault e)
                          (get-stats))} "Refresh"]
    [:div (get @app-state :stats)]])


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

(defn mount-root []
  (reagent/render [current-page] (.getElementById js/document "app")))

(defn init! []
  (hook-browser-navigation!)
  (mount-root))

(init!)