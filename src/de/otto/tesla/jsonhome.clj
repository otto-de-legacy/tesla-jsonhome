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

(defn routes [self url]
  (c/routes
    (c/GET url [] (json-home-response self))))

(defrecord JsonHome [config route-to handler]
  component/Lifecycle
  (start [self]
    (log/info "-> Starting JsonHome.")
    (let [url (or (:route-to self)
                  (get-in self [:config :config :jsonhome-url]))]
      (handler/register-handler handler (routes self url))))

  (stop [self]
    (log/info "<- Stopping JsonHome")
    self))

(defn new-jsonhome
  ([]
   (new-jsonhome nil))
  ([route-to]
   (map->JsonHome {:route-to route-to})))
