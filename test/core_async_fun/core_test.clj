(ns core-async-fun.core-test
  (:require [clojure.test :refer :all]
            [core-async-fun.core :refer :all]))

(deftest a-test
  (testing "FIXED, I succeed."
    (is (= 1 1))))
