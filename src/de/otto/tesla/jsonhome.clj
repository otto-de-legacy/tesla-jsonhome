(ns de.otto.tesla.jsonhome
  (:require [clojure.tools.logging :as log]
            [de.otto.tesla.stateful.handler :as handler]
            [de.otto.tesla.util.config-parser :as parser]
            [com.stuartsierra.component :as component]
            [compojure.core :as c]
            [clojure.data.json :as json]))

(defn prefix-keys [prefix [k v]]
  [(str prefix (name k)) v])

(defn json-response-body [configuration]
  (let [resources (get-in (parser/prop->nested-hash configuration) [:jsonhome :resources])
        prefix (:jsonhome-link-rel-prefix configuration "")]
    {:resources (into {} (map (partial prefix-keys prefix) resources))}))

(defn json-home-response [configuration]
  (let [response-body (json-response-body configuration)]
    {:status  200
     :headers {"Content-Type" "application/json-home"}
     :body    (json/write-str response-body :escape-slash false)}))

(defn routes [configuration url]
  (c/routes
    (c/GET url [] (json-home-response configuration))))

(defrecord JsonHome [config route-to handler]
  component/Lifecycle
  (start [self]
    (log/info "-> Starting JsonHome.")
    (let [configuration (:config config)
          url (or route-to (:jsonhome-url configuration))]
      (handler/register-handler handler (routes configuration url)))
    self)

  (stop [self]
    (log/info "<- Stopping JsonHome")
    self))

(defn new-jsonhome
  ([]
   (new-jsonhome nil))
  ([route-to]
   (map->JsonHome {:route-to route-to})))
