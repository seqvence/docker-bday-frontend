(ns docker-bday-frontend.chart
  (:require [reagent.core :as reagent]
            [cljsjs.d3]))

(enable-console-print!)

(def width 360)
(def height 360)
(def radius (/ (.min js/Math width height) 2))

(def color (js/d3.scale.category20b.))

(def arc  (.. (d3.svg.arc.)
              (outerRadius radius)
              (innerRadius (- radius 75))))

;(def labelArc (.. (d3.svg.arc.)
;                  (outerRadius (- radius 40))
;                  (innerRadius (- radius 40))))

(def pie (.. (d3.layout.pie.)
             (sort (clj->js nil))
             (value (fn [d]
                      (.-votes d)))))

(def tooltip (.. js/d3
                 (select "chart-container")
                 (append "div")
                 (attr "class" "tooltip")
                 (append "div")
                 (attr "class" "label")
                 (append "div")
                 (attr "class" "count")
                 (append "div")
                 (attr "class" "percent")))

(defn svg []
  (-> js/d3
   (.select "svg")
   (.append "g")
   (.attr "transform" (str "translate(" (/ width 2) "," (/ height 2) ")"))))

(defn path [parent data]
  (-> parent
    (.selectAll "path")
    (.data (pie data))
    (.enter)
    (.append "path")
    (.attr "d" arc)
    (.attr "fill" (fn [d i]
                    (color (.-label (.-data d)))))))


(defn d3-render [_]
  [:div [:svg {:width 400 :height 400}]])


(defn d3-did-mount [this]
  (let [d3data (clj->js (:languages (reagent/state this)))]
    (-> (svg)
        (path d3data))))

(defn d3-did-update [this]
  (let [[_ data] (reagent/argv this)
        d3data (clj->js (get data :languages))]
    (-> (svg)
        (path d3data))))

(defn d3-inner [data]
  (reagent/create-class {:reagent-render d3-render
                         :component-did-mount d3-did-mount
                         :component-did-update d3-did-update
                         :get-initial-state (fn [this]
                                              (reagent/set-state this data))}))