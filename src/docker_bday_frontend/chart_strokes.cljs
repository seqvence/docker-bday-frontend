(ns docker-bday-frontend.chart_strokes
  (:require [reagent.core :as reagent]
            [strokes.core :as strokes :refer [d3]]))

(def charts (atom {}))

(strokes/bootstrap)

(def width 960)
(def height 500)
(def max_bar_width 420)

(defn svg []
  (-> d3 (.select ".chart")
         (.attr {:width width :height height})))

(defn updates [svg data]
  (let [divs (-> svg
             (.selectAll "section")
             (.data (clj->js data)))
        max_vote (apply max (map :votes data))
        domain (clj->js [0 max_vote])
        range (clj->js [0 max_bar_width])
        x (-> (d3.scale.linear.)
              (.domain domain)
              (.range range))]

    (-> divs
        (.enter)
        (.append "section")
        ((fn [sec]
          (-> sec
              (.append "bar"))
          (-> sec
              (.append "description")))))

    (-> divs
        (.select "bar")
        (.style "width" (fn [d]
                          (println d)
                          (str (x (.-votes d)) "px")))
        (.text (fn [d] (.-votes d))))

    (-> divs
        (.select "description")
        (.text (fn [d] (.-label d))))))


(defn d3-render [_]
  [:div {:class "chart"}])

(defn d3-did-mount [this]
  (println "creating chart")
  (let [chart  (svg)
        d3data (:languages (reagent/state this))]
    (swap! charts assoc (reagent/dom-node this) chart)
    (-> chart
        (updates d3data))))

(defn d3-did-update [this]
  (println "updating chart")
  (let [[_ data] (reagent/argv this)
        d3data (get data :languages)
        chart (get @charts (reagent/dom-node this))]
    (-> chart
        (updates d3data))))

(defn d3-inner [data]
  (reagent/create-class {:reagent-render d3-render
                         :component-did-mount d3-did-mount
                         :component-did-update d3-did-update
                         :get-initial-state (fn [this]
                                              (reagent/set-state this data))}))