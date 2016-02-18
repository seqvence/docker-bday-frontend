(ns docker-bday-frontend.core
  (:require-macros [cljs.core.async.macros :refer [go go-loop]])
  (:require [cljs.core.async :refer [>! <! chan put!]]
            [docker-bday-frontend.map :as map]
            [docker-bday-frontend.components :as components]
            [reagent.core :as reagent]
            [reagent.session :as session]
            [goog.events :as events]
            [goog.history.EventType :as EventType]
            [secretary.core :as secretary :include-macros true]
            [ajax.core :refer [GET POST]]
            [gmaps.location :as glocation])
  (:import goog.History))

(enable-console-print!)

; ugly variable required to reference the map
(defonce app-state (reagent/atom {:stats "Initial state"}))

(def map-data
  (reagent/atom
  {:center {:lat 37.7833 :lng -30.431297}
   :disableDefaultUI false
   :zoom 2
   :mapTypeId google.maps.MapTypeId.ROADMAP

   :markers #{{:position {:lat 37.7833
                          :lng -122.431297}
               :title "Docker HQ"}}}))

;;-------------------------
;; Backend comm.

(defn clone-js [jsobj]
  (.parse js/JSON (.stringify js/JSON jsobj)))

(defn add-submission [submission]
  (println (str "adding submission for " (get submission "name")))
  (go (map/submission-marker (get submission "name") (get-in (js->clj (<! (map/get-location (get submission "location")))) [:result 0 "geometry" "location"]))))

(defn add-marker [submission]
  (println (str "adding submission for " (get submission "name")))
  (go
    (let [pos (clone-js (get-in (js->clj (<! (map/get-location (get submission "location")))) [:result 0 "geometry" "location"]))
          marker {:position {:lat (.-lat pos)
                             :lng (.-lng pos)}
                  :title (get submission "name")}
          all-markers (conj (get @map-data :markers) marker)]
      (swap! map-data assoc :markers all-markers))))

(defn response-handler [response]
  (println (str "Received response from backend: " response))
  (swap! app-state assoc :stats response)
  (doseq [submission response]
    (add-marker submission)))

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
  [:div
    [components/header]
    [:button {:on-click (fn [e] (.preventDefault e)
              (get-stats))} "Refresh"]
    [map/map-view @map-data]])

(defn instructions []
  [:div
   [components/header]
   [:div "Instructions content"]])


(defn current-page []
  [:div [(session/get :current-page)]])

;;-------------------------
;; Routes

(secretary/set-config! :prefix "#")

(secretary/defroute "/" []
                    (session/put! :current-page home-page))

(secretary/defroute "/instructions" []
                    (session/put! :current-page instructions))


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