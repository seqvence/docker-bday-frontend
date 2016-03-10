(ns docker-bday-frontend.components)

(defn header []
  [:div {:class "header clearfix"}
   [:nav
     [:ul {:class "nav nav-pills pull-right"}
      [:li [:a {:href "#/"} "Home"]]
      [:li [:a {:href "#/instructions"} "Instructions"]]]]
   [:a {:href "#" :class "pull-left" }
    [:img {:src "/birthday.jpg" :height 50}]]
   [:h3 {:class "text-muted"} "Docker birthday challenge"]])