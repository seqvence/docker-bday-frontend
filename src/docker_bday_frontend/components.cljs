(ns docker-bday-frontend.components)

(defn header []
  [:div {:class "header clearfix"}
   [:nav
     [:ul {:class "nav nav-pills pull-right"}
      [:li [:a {:href "#/"} "Home"]]]]
   [:h3 {:class "text-muted"} "Docker competition"]])