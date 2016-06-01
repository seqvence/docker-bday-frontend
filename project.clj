(defproject docker-bday-frontend "0.1.0-SNAPSHOT"
  :description "FIXME: write this!"
  :url "http://example.com/FIXME"
  :license {:name "Eclipse Public License"
            :url "http://www.eclipse.org/legal/epl-v10.html"}

  :min-lein-version "2.5.3"
  
  :dependencies [[org.clojure/clojure "1.7.0"]
                 [org.clojure/clojurescript "1.7.170"]
                 [org.clojure/core.async "0.2.374"
                  :exclusions [org.clojure/tools.reader]]
                 [reagent "0.6.0-alpha"]
                 [reagent-utils "0.1.5"]
                 [reagent-forms "0.5.9"]
                 [secretary "1.2.3"]
                 [cljs-ajax "0.3.14"]
                 [figwheel-sidecar "0.5.0"]
                 [cljsjs/google-maps "3.18-1"]
                 [gmaps-cljs "0.0.2"]
                 [cljsjs/d3 "3.5.7-1"]
                 [strokes "0.5.2-SNAPSHOT"]
                 [rigui "0.5.0"]
                 [cljsjs/react-bootstrap "0.28.1-1"]]

  :plugins [[lein-cljsbuild "1.1.2" :exclusions [[org.clojure/clojure]]]]

  :source-paths ["src"]

  :clean-targets ^{:protect false} ["resources/public/js/compiled" "target"]

  :cljsbuild {:builds
              [
               {:id "dev"
                :source-paths ["src"  "checkouts/gmaps-cljs/src"]

                :figwheel {:on-jsload "docker-bday-frontend.core/on-js-reload"}

                :compiler {:main docker-bday-frontend.core
                           :asset-path "js/compiled/out"
                           :cache-analysis true
                           :output-to "resources/public/js/compiled/docker_bday_frontend.js"
                           :output-dir "resources/public/js/compiled/out"
                           :foreign-libs [{:file "markerclusterer/markerclusterer.js"
                                           :provides ["markerclusterer"]}]
                           :source-map-timestamp true}}

               {:id "min"
                :source-paths ["src" "checkouts/gmaps-cljs/src"]
                :compiler {:output-to "resources/public/js/compiled/docker_bday_frontend.min.js"
                           :main docker-bday-frontend.core
                           :optimizations :whitespace
                           :pretty-print false}}
               ]}

  :figwheel {:css-dirs ["resources/public/css"]})
