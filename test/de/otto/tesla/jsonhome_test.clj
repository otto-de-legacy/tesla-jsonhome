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

(defn- serverless-system [runtime-config]
  (-> (system/base-system runtime-config)
      (assoc :jsonhome (c/using (jsonhome/new-jsonhome "/jsonhome/") [:config :handler]))
      (dissoc :server)))

(deftest ^:unit json-home-response-test
  (testing "without link-rel-prefix"
    (u/with-started [started (serverless-system {:status-url "/my-status"
                                                 :health-url "/my-health"})]
                    (let [page (jsonhome/json-home-response started {})
                          body (json/read-str (:body page) :key-fn keyword)
                          _ (log/info body)]
                      (testing "it shows status url by default"
                        (is (= (get-in body [:resources :status])
                               {:href "/my-status"})))
                      (testing "it shows healthcheck url by default"
                        (is (= (get-in body [:resources :healthcheck])
                               {:href "/my-health"}))))))
  (testing "with link-rel-prefix"
    (u/with-started [started (serverless-system {:status-url "/my-status"
                                                 :health-url "/my-health"
                                                 :jsonhome-link-rel-prefix "http://spec.example.com/link-rel/"})]
                    (let [page (jsonhome/json-home-response started {})
                          body (json/read-str (:body page) :key-fn keyword)
                          _ (log/info body)]
                      (testing "all resources have a prefix"
                        (is (= (keys (:resources body))
                               [:http://spec.example.com/link-rel/status :http://spec.example.com/link-rel/healthcheck]))))))
  (testing "with additional resources"
    (u/with-started [started (serverless-system {:status-url "/my-status"
                                                 :health-url "/my-health"})]
                    (let [page (jsonhome/json-home-response started {:foo {:href "/abc"}
                                                                     :bar {:title "Xyz AbC"}})
                          body (json/read-str (:body page) :key-fn keyword)
                          _ (log/info body)]
                      (testing "default and additional resources are returned"
                        (is (= (keys (:resources body))
                               [:foo :bar :status :healthcheck])))
                      (testing "it shows the complete additional resources"
                        (is (= (get-in body [:resources :foo])
                               {:href "/abc"}))
                        (is (= (get-in body [:resources :bar])
                               {:title "Xyz AbC"})))))))

(deftest ^:integration should-serve-jsonhome-under-given-url
  (testing "use the default url"
    (u/with-started [started (serverless-system {})]
                    (let [handlers (handler/handler (:handler started))]
                      (is (= (:status (handlers (mock/request :get "/jsonhome/")))
                             200))))))
