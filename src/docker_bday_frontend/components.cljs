(ns docker-bday-frontend.components
  (:require [reagent.core :as reagent]
            [cljsjs.react-bootstrap]))


(def Button (reagent/adapt-react-class (aget js/ReactBootstrap "Button")))
(def Modal (reagent/adapt-react-class (aget js/ReactBootstrap "Modal")))
(def ModalBody (reagent/adapt-react-class (aget js/ReactBootstrap "ModalBody")))
(def ModalHeader (reagent/adapt-react-class (aget js/ReactBootstrap "ModalHeader")))

(defn header []
  [:div {:class "header clearfix"}
   [:nav
     [:ul {:class "nav nav-pills pull-right"}
      [:li [:a {:href "#/"} "Home"]]
      [:li [:a {:href "#/instructions"} "Instructions"]]]]
   [:a {:href "#" :class "pull-left" }
    [:img {:src "/birthday.jpg" :height 50}]]
   [:h3 {:class "text-muted"} "Docker birthday challenge"]])

(defn submission-info [rtr info]
  (let [submission (reagent/atom "Your submission id")]
    (fn []
      [:div
       [:input {:type "text"
                :value @submission
                :on-change #(reset! submission (-> % .-target .-value))}]
       [Button {:bsSize "small"
                :bsStyle "primary"
                :on-click (fn [e] (.preventDefault e) (rtr @submission))} "Get submission"]
       [:div (str "Submission data: " info)]])))

(defn submission-modal [get-submission app-state]
  (fn []
    [Modal {:show (get @app-state :show-modal) :onHide (fn []
                                                         (swap! app-state assoc :show-modal false))}
     [ModalHeader {:closeButton true} "Submission status"]
     [ModalBody [submission-info get-submission (get @app-state :submission-info)]]]))