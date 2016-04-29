(ns rfqservice.reducer-test
  (:require [clojure.test :refer :all]
            [rfqservice.core :refer [orders quote-for]])
  (:import java.util.Optional
           rfqservice.core.Quote
           rfqservice.reducer.ReducerRfqService))

(deftest matching-quote
  (testing "Check quote for 200 USD returns expected values"
    (let [quote (quote-for (ReducerRfqService. 0.02M orders) "USD" 200)]
      (is (= true (instance? Optional quote)))
      (is (= true (instance? Quote (.orElse quote :unfulfilled))))
      (is (= 232.69 (:bid (.orElse quote :unfulfilled))))
      (is (= 232.75 (:ask (.orElse quote :unfulfilled))))))

  (testing "Check quote for 100 USD returns expected values"
    (let [quote (quote-for (ReducerRfqService. 0.02M orders) "USD" 100)]
      (is (instance? Optional quote))
      (is (= 232.68 (:bid (.orElse quote :unfulfilled))))
      (is (= 232.76 (:ask (.orElse quote :unfulfilled)))))))

(deftest unfulfilled-quote
  (testing "Check quote for 300 USD returns no matching order"
    (let [quote (quote-for (ReducerRfqService. 0.02M orders) "USD" 300)]
      (is (instance? Optional quote))
      (is (= :unfulfilled (.orElse quote :unfulfilled)))
      (is (not (.isPresent quote)))))

  (testing "Check quote for 300 GBP returns no matching order"
    (let [quote (quote-for (ReducerRfqService. 0.02M orders) "GBP" 300)]
      (is (instance? Optional quote))
      (is (= :unfulfilled (.orElse quote :unfulfilled)))
      (is (not (.isPresent quote))))))
