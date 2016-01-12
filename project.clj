(defproject de.otto/tesla-jsonhome "0.2.1"
            :description "Addon to https://github.com/otto-de/tesla-microservice to render a JSON-HOME document."
            :url "https://github.com/otto-de/tesla-jsonhome"
            :license {:name "Apache License 2.0"
                      :url  "http://www.apache.org/license/LICENSE-2.0.html"}
            :scm {:name "git"
                  :url  "https://github.com/otto-de/tesla-jsonhome"}
            :dependencies [[org.clojure/clojure "1.7.0"]
                           [org.clojure/data.json "0.2.6"]]
            :profiles {:provided {:dependencies [[de.otto/tesla-microservice "0.1.25"]
                                                 [com.stuartsierra/component "0.3.1"]]}
                       :dev      {:dependencies [[ring-mock "0.1.5"]]
                                  :plugins [[lein-ancient "0.6.8"]]}})
