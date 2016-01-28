(ns docker-bday-frontend.prod
  (:require [docker-bday-frontend.core :as core]))

;;ignore println statements in prod
(set! *print-fn* (fn [& _]))

(core/init!)
