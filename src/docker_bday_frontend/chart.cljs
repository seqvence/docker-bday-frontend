(ns docker-bday-frontend.chart
  (:require [reagent.core :as reagent]
            [cljsjs.d3]))

(enable-console-print!)

(def chart-state (reagent/atom {:name "Language stats"
                                :x 35
                                :y 35
                                :r 15
                                :color "red"}))

(def chart-data [{:votes 10 :label "Python"}
                 {:votes 30 :label "dfdf"}
                 {:votes 150 :label "sdfsdfw"}])

(def width 360)
(def height 360)
(def radius (/ (.min js/Math width height) 2))

(def color (js/d3.scale.category20b.))

(def arc  (.. (d3.svg.arc.)
              (outerRadius (- radius 10))
              (innerRadius 0)))

;(def labelArc (.. (d3.svg.arc.)
;                  (outerRadius (- radius 40))
;                  (innerRadius (- radius 40))))

(def pie (.. (d3.layout.pie.)
             (sort (clj->js nil))
             (value (fn [d]
                      (.-votes d)))))

;(def svg (.. js/d3
;             (select "svg")
;             (append "g")
;             (attr "transform" (str "translate(" (/ width 2) "," (/ height 2) ")"))))
;
;(def path (.. svg
;              (selectAll "path")
;              (data (pie (clj->js chart-data)))
;              (enter)
;              (append "path")
;              (attr "d" arc)))

(println (clj->js chart-data))

(defn d3-render [_]
  [:div [:svg {:width 400 :height 400}]])


(defn d3-did-mount [this]
  (println "mounting charts")
  (let [d3data (clj->js (reagent/state this))]
    (.. js/d3
        (select "svg")
        (append "g")
        (attr "transform" (str "translate(" (/ width 2) "," (/ height 2) ")"))
        (selectAll "path")
        (data (pie (clj->js chart-data)))
        (enter)
        (append "path")
        (attr "d" arc)
        (attr "fill" (fn [d i]
                       (color (.-label (.-data d))))))))

(defn d3-did-update [this]
  (let [[_ data] (reagent/argv this)
        d3data (clj->js data)]
    (.. js/d3
        (selectAll "circle")
        (data d3data)
        (attr "cx" (fn [d] (.-x d)))
        (attr "cy" (fn [d] (.-y d)))
        (attr "r" (fn [d] (.-r d))))))

(defn d3-inner [data]
  (reagent/create-class {:reagent-render d3-render
                         :component-did-mount d3-did-mount
                         :component-did-update d3-did-update
                         :get-initial-state (fn [this]
                                              (reagent/set-state this data))}))