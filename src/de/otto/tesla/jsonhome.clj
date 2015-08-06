(ns de.otto.tesla.jsonhome
  (:require [clojure.tools.logging :as log]
            [de.otto.tesla.stateful.handler :as handler]
            [de.otto.tesla.util.config-parser :as parser]
            [com.stuartsierra.component :as component]
            [compojure.core :as c]
            [clojure.data.json :as json]))

(defn prefix-keys [prefix [k v]]
  [(str prefix (name k)) v])

(defn json-response-body [config]
  (let [resources (get-in (parser/prop->nested-hash config) [:jsonhome :resources])
        prefix (:jsonhome-link-rel-prefix config "")]
    {:resources (into {} (map (partial prefix-keys prefix) resources))}))

(defn json-home-response [self]
  (let [response-body (json-response-body (get-in self [:config :config]))]
    {:status  200
     :headers {"Content-Type" "application/json-home"}
     :body    (json/write-str response-body :escape-slash false)}))

(defn routes [self]
  (c/routes
    (c/GET (:route-to self) [] (json-home-response self))))

(defrecord JsonHome [config handler]
  component/Lifecycle
  (start [self]
    (log/info "-> Starting JsonHome.")
    (handler/register-handler handler (routes self)))

  (stop [self]
    (log/info "<- Stopping JsonHome")
    self))

(defn new-jsonhome [route-to]
  (map->JsonHome {:route-to route-to}))
