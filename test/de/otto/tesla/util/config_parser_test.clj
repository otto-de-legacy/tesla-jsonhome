(ns de.otto.tesla.util.config-parser-test
  (:require [clojure.test :refer :all]
            [de.otto.tesla.util.config-parser :as config]))

(deftest prop->nested-hash-test
         (testing "it "
                  (is (= nil
                         (config/prop->nested-hash {})))
                  (is (= {:foo "bar"}
                         (config/prop->nested-hash {:foo "bar"})))
                  (is (= {:foo {:bar "xyz"}}
                         (config/prop->nested-hash {:foo-bar "xyz"})))
                  (is (= {:foo {:bar {:baz "xyz"}}}
                         (config/prop->nested-hash {:foo-bar-baz "xyz"})))
                  (is (= {:foo {:bar "xyz"
                                :baz "abc"}
                          :foz "def"}
                         (config/prop->nested-hash {:foo-bar "xyz"
                                                      :foo-baz "abc"
                                                      :foz "def"})))
                  (is (= {:foo {:foz {:bar "xyz"
                                      :baz "abc"}}
                          :foz {:foo "123"}}
                         (config/prop->nested-hash {:foo-foz-bar "xyz"
                                                      :foo-foz-baz "abc"
                                                      :foz-foo "123"})))))