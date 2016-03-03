(ns docker-bday-frontend.chart
  (:require [reagent.core :as reagent]
            [cljsjs.d3]))

(enable-console-print!)

(def charts (atom {}))

(def width 360)
(def height 360)
(def radius (/ (.min js/Math width height) 2))

(def color (js/d3.scale.category20b.))

(def arc  (.. (d3.svg.arc.)
              (outerRadius radius)))

(def labelArc (.. (d3.svg.arc.)
                  (outerRadius (- radius 40))
                  (innerRadius (- radius 40))))

(def pie (.. (d3.layout.pie.)
             (sort (clj->js nil))
             (value (fn [d]
                      (.-votes d)))))

(defn svg []
  (-> js/d3
   (.select "svg")
   (.append "g")
   (.attr "transform" (str "translate(" (/ width 2) "," (/ height 2) ")"))))

(defn path [parent data]
  (let [g (-> parent
              (.selectAll "path")
              (.data (pie data))
              (.enter))]
    (-> g
        (.append "path")
        (.attr "d" arc)
        (.attr "fill" (fn [d i]
                        (color (.-label (.-data d))))))
    (-> g
        (.append "text")
        (.attr "transform" (fn [d]
                             (str "translate(" (.centroid labelArc d) ")")))
        (.attr "dy"  ".35em")
        (.text (fn [d]
                 (.-votes (.-data d)))))
    g))


(defn d3-render [_]
  [:div [:svg {:width width :height height}]])


(defn d3-did-mount [this]
  (println "creating chart")
  (let [d3data (clj->js (:languages (reagent/state this)))
        chart  (svg)]
    (swap! charts assoc (reagent/dom-node this) chart)
    (path chart d3data)))

(defn d3-did-update [this]
  (println "updating chart")
  (let [[_ data] (reagent/argv this)
        d3data (clj->js (get data :languages))
        chart (get @charts (reagent/dom-node this))]
    (-> (svg)
        (path d3data))))

(defn d3-inner [data]
  (reagent/create-class {:reagent-render d3-render
                         :component-did-mount d3-did-mount
                         :component-did-update d3-did-update
                         :get-initial-state (fn [this]
                                              (reagent/set-state this data))}))