(ns de.otto.tesla.jsonhome
  (:require [clojure.tools.logging :as log]
            [de.otto.tesla.stateful.handler :as handler]
            [com.stuartsierra.component :as component]
            [compojure.core :as c]
            [clojure.data.json :as json]))

(defn status-resource [config]
  {(str (:jsonhome-link-rel-prefix config "") "status")
   {:href (:status-url config)}})

(defn health-resource [config]
  {(str (:jsonhome-link-rel-prefix config "") "healthcheck")
   {:href (:health-url config)}})

(defn json-response-body [config additional-resources]
  {:resources (-> {}
                  (merge (health-resource config))
                  (merge (status-resource config))
                  (merge additional-resources))})

(defn json-home-response [self additional-resources]
  (let [response-body (json-response-body (get-in self [:config :config]) additional-resources)]
    {:status  200
     :headers {"Content-Type" "application/json-home"}
     :body    (json/write-str response-body :escape-slash false)}))

(defn routes [self additional-resources]
  (c/routes
    (c/GET (:route-to self) [] (json-home-response self additional-resources))))

(defrecord JsonHome [config handler additional-resources]
  component/Lifecycle
  (start [self]
    (log/info "-> Starting JsonHome.")
    (handler/register-handler handler (routes self additional-resources)))

  (stop [self]
    (log/info "<- Stopping JsonHome")
    self))

(defn new-jsonhome
  ([route-to]
   (map->JsonHome {:route-to             route-to}))
  ([route-to additional-resources]
   (map->JsonHome {:route-to             route-to
                   :additional-resources additional-resources})))
