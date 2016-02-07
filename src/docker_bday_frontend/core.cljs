(ns docker-bday-frontend.core
  (:require [reagent.core :as reagent]
            [reagent.session :as session]
            [goog.events :as events]
            [goog.history.EventType :as EventType]
            [secretary.core :as secretary :include-macros true]
            [ajax.core :refer [GET POST]])
  (:import goog.History))

(enable-console-print!)

(defonce app-state (atom {:text "Initial state"}))

;;--------------------------
;; Pages

(defn home-page []
  [:div (get @app-state :text)])


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