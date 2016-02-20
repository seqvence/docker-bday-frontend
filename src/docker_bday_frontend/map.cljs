(ns docker-bday-frontend.map
  (:require-macros [cljs.core.async.macros :refer [go go-loop]])
  (:require [reagent.core :as reagent]
            [gmaps.core :as gmaps]
            [cljs.core.async :refer [>! <! chan put! close!]]))


;;
;; Map state
;;
(def map-data
  (reagent/atom
    {:center {:lat 37.7833 :lng -30.431297}
     :disableDefaultUI false
     :zoom 2
     :mapTypeId google.maps.MapTypeId.ROADMAP

     :markers #{{:position {:lat 37.7833
                            :lng -122.431297}
                 :title "Docker HQ"}}}))


(defn get-geocode [location]
  (let [res (chan)
        geocoder (google.maps.Geocoder.)]
    (.geocode geocoder
              location
              (fn [result status]
                (put! res {:status status :result result})))
    res))

(defn get-location
  [site]
  (let [out (chan)]
    (go (let [address (clj->js {"address" site})
              geo (<! (get-geocode address))]
          (>! out geo)))
    out))

(defn clone-js [jsobj]
  (.parse js/JSON (.stringify js/JSON jsobj)))

(defn add-marker [submission]
  (println (str "adding submission for " (get submission "name")))
  (go
    (let [pos (clone-js (get-in (js->clj (<! (get-location (get submission "location")))) [:result 0 "geometry" "location"]))
          marker {:position {:lat (.-lat pos)
                             :lng (.-lng pos)}
                  :title (get submission "name")}
          all-markers (conj (get @map-data :markers) marker)]
      (swap! map-data assoc :markers all-markers))))

;(defn create-lat-lng [lat lng]
;  (google.maps.LatLng. lat lng))
;
;(defn create-marker [name latlng]
;  (.setMap (google.maps.Marker. (clj->js {"position" latlng "title" name})) *maper* ))
;
;(defn get-marker [name latlng]
;  (google.maps.Marker. (clj->js {"position" latlng "title" name})))
;
;(defn create-infowindow [id]
;  (js/google.maps.InfoWindow. (clj->js {"content" (str "<div id='info-"id"'></div>")})))
;
;(defn submission-info-window [name]
;  [:div
;   [:div name]])

;(defn submission-marker [name location]
;  (let [marker (get-marker name location)
;        infowindow (create-infowindow name)]
;
;    (js/google.maps.event.addListener
;      marker
;      "click"
;      (fn []
;        (.open infowindow *maper* marker)
;        ))
;
;    (js/google.maps.event.addListener
;      infowindow
;      "domready"
;      (fn []
;        (reagent/render (submission-info-window name) (js/document.getElementById (str "info-" name))))
;        )
;
;  (.setMap marker *maper*)))
;
;(defn add-mark []
;  (let [marker {:position {:lat 37.7833
;                           :lng -118.431297}
;                :title "Second"}
;        all-markers (conj (get @map-data :markers) marker)]
;    (swap! map-data assoc :markers all-markers)
;    (println @map-data)))