(ns de.otto.tesla.jsonhome-test
  (:require [clojure.test :refer :all]
            [com.stuartsierra.component :as c]
            [de.otto.tesla.system :as system]
            [de.otto.tesla.stateful.handler :as handler]
            [de.otto.tesla.jsonhome :as jsonhome]
            [de.otto.tesla.util.test-utils :as u]
            [clojure.tools.logging :as log]
            [clojure.data.json :as json]
            [ring.mock.request :as mock]))

(defn- serverless-system
  ([runtime-config]
   (serverless-system nil runtime-config))
  ([url runtime-config]
   (-> (system/base-system runtime-config)
       (assoc :jsonhome (c/using (jsonhome/new-jsonhome url) [:config :handler]))
       (dissoc :server))))

(deftest ^:unit json-home-response-test
  (testing "without link-rel-prefix"
    (u/with-started [started (serverless-system {:jsonhome-resources-status-href "/my-status"
                                                 :jsonhome-resources-health-href "/my-health"})]
                    (let [page (jsonhome/json-home-response started)
                          body (json/read-str (:body page) :key-fn keyword)]
                      (testing "it shows the configured resources"
                        (is (= (keys (:resources body))
                               [:status
                                :health]))
                        (is (= (get-in body [:resources :status])
                               {:href "/my-status"}))
                        (is (= (get-in body [:resources :health])
                               {:href "/my-health"}))))))
  (testing "with link-rel-prefix"
    (u/with-started [started (serverless-system {:jsonhome-resources-status-href "/my-status"
                                                 :jsonhome-resources-health-href "/my-health"
                                                 :jsonhome-link-rel-prefix "http://spec.example.com/link-rel/"})]
                    (let [page (jsonhome/json-home-response started)
                          body (json/read-str (:body page) :key-fn keyword)]
                      (testing "all resources have a prefix"
                        (is (= (keys (:resources body))
                               [:http://spec.example.com/link-rel/status
                                :http://spec.example.com/link-rel/health])))))))

(deftest ^:integration should-serve-jsonhome-under-given-url
  (testing "prefer the passed url over the configured url"
    (u/with-started [started (serverless-system "/jsonhome/" {:jsonhome-url "/some-other/jsonhome/"})]
                    (let [handlers (handler/handler (:handler started))]
                      (is (= (:status (handlers (mock/request :get "/jsonhome/")))
                             200)))))
  (testing "use the configured url in case no url is passed"
    (u/with-started [started (serverless-system nil {:jsonhome-url "/some-other/jsonhome/"})]
                    (let [handlers (handler/handler (:handler started))]
                      (is (= (:status (handlers (mock/request :get "/some-other/jsonhome/")))
                             200))))))
