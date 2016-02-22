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

(defn add-marker [submission]
  (println (str "Adding marker for " (get submission "name")))
  (let [marker {:position (get submission "coordinates")
                :title (get submission "name")}
        all-markers (conj (get @map-data :markers) marker)]
    (swap! map-data assoc :markers all-markers)))