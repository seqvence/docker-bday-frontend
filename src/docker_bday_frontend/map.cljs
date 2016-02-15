(ns docker-bday-frontend.map
  (:require-macros [cljs.core.async.macros :refer [go go-loop]])
  (:require [reagent.core :as reagent]
            [cljs.core.async :refer [>! <! chan put! close!]]))

(declare *maper*)

(defn map-render []
  [:div {:style {:height "500px" :width "700px"}}])

(defn map-did-mount [this]
  (let [map-canvas (reagent/dom-node this)
        map-options (clj->js {"center" (google.maps.LatLng. 37.7833, -122.4167)
                              "zoom" 5})]
    (set! *maper* (google.maps.Map. map-canvas map-options))))

(defn map-component []
  (reagent/create-class {:reagent-render map-render
                         :component-did-mount map-did-mount}))

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

(defn create-lat-lng [lat lng]
  (google.maps.LatLng. lat lng))

(defn create-marker [name latlng]
  (println "Adding marker to map")
  (.setMap (google.maps.Marker. (clj->js {"position" latlng "title" name})) *maper* ))