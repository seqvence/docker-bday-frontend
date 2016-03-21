(ns docker-bday-frontend.map
  (:require-macros [cljs.core.async.macros :refer [go go-loop]])
  (:require [reagent.core :as reagent]
            [gmaps.core :as gmaps]
            [cljs.core.async :refer [>! <! chan put! close!]]))


;;
;; Map state
;;
(def mapdata
  (reagent/atom
    {:center {:lat 37.7833 :lng -30.431297}
     :disableDefaultUI false
     :styles [{:featureType "poi"
               :stylers [{:visibility "off"}]}]
     :zoom 2
     :mapTypeId google.maps.MapTypeId.ROADMAP
     :clustering true
     :markers #{}}))

(defn infowindow-content [id name twitter]
  (let [twitter-link (str "https://twitter.com/" (clojure.string/replace-first twitter "@" ""))]
    (str "<div id='info-" id "'>Name: " name "<br>Twitter: <a href='" twitter-link "'>"  twitter "</a></div>")))

(defn add-marker [submission]
  ;(println (str "Adding marker for " (get submission "id")))
  (let [marker {:position (get submission "coordinates")
                :title (get submission "id")
                :infowindow (infowindow-content (get submission "id") (get submission "name") (get submission "twitter"))}
        all-markers (conj (get @mapdata :markers) marker)]
    (swap! mapdata assoc :markers all-markers)))