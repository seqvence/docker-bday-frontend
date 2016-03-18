(ns docker-bday-frontend.components
  (:require [reagent.core :as reagent]
            [cljsjs.react-bootstrap]
            [clojure.string :as string]))


(def Button (reagent/adapt-react-class (aget js/ReactBootstrap "Button")))
(def Modal (reagent/adapt-react-class (aget js/ReactBootstrap "Modal")))
(def ModalBody (reagent/adapt-react-class (aget js/ReactBootstrap "ModalBody")))
(def ModalHeader (reagent/adapt-react-class (aget js/ReactBootstrap "ModalHeader")))

(defn header []
  [:div {:class "header clearfix"}
   [:nav
     [:ul {:class "nav nav-pills pull-right"}
      [:li [:a {:target "_blank"  :href "https://github.com/docker/docker-birthday-3/blob/master/tutorial.md"} "Tutorial"]]]]
   [:a {:href "#" :class "pull-left" }
    [:img {:src "/birthday.jpg" :height 50}]]
   [:h3 {:class "text-muted"} "Docker Birthday #3"]])

(defn submission-modal [get-submission app-state]
  (let [submission-id (reagent/atom nil)
        submission-info (reagent/atom {})
        show-modal (reagent/atom false)
        is-loading (reagent/atom false)
        has-error (reagent/atom "")
        error-msg (reagent/atom "")]
    (fn []
      [:div {:class @has-error :style {:padding-bottom "1em"}}
          [:div {:class "input-group"}
            [:input {:type "text"
                     :class "form-control"
                     :placeholder "Your submission id"
                     :on-change #(reset! submission-id (-> % .-target .-value))}]
            [:span {:class "input-group-btn"}
              [Button {:class "btn btn-default"
                       :disabled @is-loading
                       :on-click (fn [] (do
                                         (reset! is-loading true)
                                         (get-submission @submission-id (fn [response]
                                                                          (println response)
                                                                          (reset! is-loading false)
                                                                          (if (get response "response")
                                                                            (do
                                                                              (reset! has-error "")
                                                                              (reset! error-msg "")
                                                                              (reset! submission-info (get response "response"))
                                                                              (reset! show-modal true))
                                                                            (do
                                                                              (reset! has-error "has-error")
                                                                              (reset! error-msg "Could not retrieve submission")))))))}
              "Submission status"]]]
        [:p {:class "help-block"} @error-msg]
        [Modal {:show @show-modal :onHide #(reset! show-modal false)}
         [ModalHeader {:closeButton true} (str "Submission " @submission-id)]
         [ModalBody
          [:div {:id "submission-info"}
           [:dl {:class "dl-horizontal"}
            [:dt "Name"]
            [:dd (get @submission-info "name")]
            [:dt "Twitter handle"]
            [:dd [:a {:href (str "https://twitter.com/" (get @submission-info "twitter"))} (get @submission-info "twitter")]]
            [:dt "Location"]
            [:dd (get @submission-info "location")]
            [:dt "Status"]
            [:dd (get @submission-info "status")]
            [:dt "Status message"]
            [:dd (get @submission-info "statusmsg")]
            [:dt "Programming language"]
            [:dd (get @submission-info "vote")]
            [:dt "Docker images"]
            [:dd (string/join "," (get @submission-info "repo"))]
            [:dt "Tweet link"]
            [:dd [:a {:href (get @submission-info "tweetmsg")} "Share with your friends"]]]]]]])))