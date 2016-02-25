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
     :clustering true
     :markers #{}}))

(defn infowindow-content [id name twitter]
  (str "<div id='info-" id "'>Name: " name "<br>Twitter: " twitter "</div>"))

(defn add-marker [submission]
  ;(println (str "Adding marker for " (get submission "id")))
  (let [marker {:position (get submission "coordinates")
                :title (get submission "id")
                :icon "/docker.ico"
                :infowindow (infowindow-content (get submission "id") (get submission "name") (get submission "twitter"))}
        all-markers (conj (get @map-data :markers) marker)]
    (swap! map-data assoc :markers all-markers)))