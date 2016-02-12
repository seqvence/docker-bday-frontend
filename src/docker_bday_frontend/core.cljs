(ns docker-bday-frontend.core
  (:require-macros [cljs.core.async.macros :refer [go go-loop]])
  (:require [cljs.core.async :refer [>! <! chan put!]]
            [docker-bday-frontend.map :as map]
            [reagent.core :as reagent]
            [reagent.session :as session]
            [goog.events :as events]
            [goog.history.EventType :as EventType]
            [secretary.core :as secretary :include-macros true]
            [ajax.core :refer [GET POST]])
  (:import goog.History))

(enable-console-print!)

; ugly variable required to reference the map
(defonce app-state (atom {:stats "Initial state"}))

;;-------------------------
;; Backend comm.

(defn add-submission [submission]
  (println (str "adding submission for " (get submission "name")))
  (go (map/create-marker (get submission "name") (get-in (js->clj (<! (map/get-location (get submission "location")))) [:result 0 "geometry" "location"]))))

(defn response-handler [response]
  (println (str "Received response from backend: " response))
  (swap! app-state assoc :stats response)
  (doseq [submission response]
    (add-submission submission)))

(defn error-handler [{:keys [status status-text]}]
  (println (str "something bad happened: " status " " status-text)))

(defn get-stats []
  (println (str "Retrieving stats"))
  (GET "/stats"
       {:handler response-handler
        :error-handler error-handler
        :format :json
        :response-format :json}))

(defn print-loc [address]
  (println "retrieveing location")
  (go (println (get-in (js->clj (<! (map/get-location address))) [:result 0 "geometry" "location"]))))


;;--------------------------
;; Pages

(defn home-page []
  (get-stats)
  [:div
    [map/map-component]
    [:button {:on-click (fn [e] (.preventDefault e)
                          (get-stats))} "Refresh"]
    [:button {:on-click (fn [e] (.preventDefault e)
                          (map/create-marker "test" (map/create-lat-lng "50" "50")))} "Create"]
    [:button {:on-click (fn [e] (.preventDefault e)
                          (print-loc "1600 Amphitheatre Parkway, Mountain View, CA"))} "Geocode"]])
    ;[:div (get @app-state :stats)]

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