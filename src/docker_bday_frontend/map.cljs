(ns docker-bday-frontend.map
  (:require-macros [cljs.core.async.macros :refer [go]])
  (:require [reagent.core :as reagent]
            [cljs.core.async :refer [>! <! chan put!]]))

(declare *maper*)

(defn map-render []
  [:div {:style {:height "300px" :width "600px"}}])

(defn map-did-mount [this]
  (let [map-canvas (reagent/dom-node this)
        map-options (clj->js {"center" (google.maps.LatLng. 52.3667, 4.9000)
                              "zoom" 3})]
    (set! *maper* (js/google.maps.Map. map-canvas map-options))))

(defn map-component []
  (reagent/create-class {:reagent-render map-render
                         :component-did-mount map-did-mount}))


(defn geocode-to-location
  [geo]
  (let [pos (-> geo .-geometry .-location)]
    {:lat (.-k pos) :lng (.-B pos)}))

;(defn geocode-result [result status]
;  (let [location (js->clj result)]
;  (println (get-in location [0 "geometry" "location"]))))
;
;(defn geocode-address [address]
;  (println (str "Geocoding " address))
;  (let [geocoder (google.maps.Geocoder.)]
;    (.geocode geocoder (clj->js {"address" address}) geocode-result)))

(defn get-geocode [location]
  (println (str "Performing get-geocode " location))
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
  (js/google.maps.LatLng. lat lng))

(defn create-marker []
  (println "Adding marker to map")
  (.setMap (js/google.maps.Marker. (clj->js {"position" (create-lat-lng 52.3667 4.9002) "title" "test"})) *maper* ))