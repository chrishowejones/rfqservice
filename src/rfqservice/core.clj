(ns rfqservice.core
  (:import [java.util Optional])
  (:gen-class))

(def orders
  [{:direction :buy :price 232.71M :currency "USD" :amount 200}
   {:direction :sell :price 232.74M :currency "USD" :amount 100}
   {:direction :sell :price 232.73M :currency "USD" :amount 200}
   {:direction :buy :price 232.71M :currency "USD" :amount 500}
   {:direction :buy :price 232.70M :currency "USD" :amount 100}
   {:direction :sell :price 232.75M :currency "USD" :amount 200}
   {:direction :buy :price 232.69M :currency "USD" :amount 500}
   {:direction :sell :price 232.76M :currency "USD" :amount 300}
   {:direction :buy :price 232.70M :currency "USD" :amount 200}])

(defn orders-for
  [currency orders]
  (filter #(= currency (:currency %)) orders))

(defprotocol RfqService
  "Quote service"
  (quote-for [this ^String currency ^Double amount]))

(defrecord Quote [^Double bid ^Double ask])

(defmulti update-quote
  (fn [_ order _] (:direction order)))

(defmethod update-quote
  :buy
  [{:keys [bid ask] :as quote} {:keys [price amount]} x-amount]
  (let [adjusted-price (- price x-amount)]
    (if (or
         (= bid :unfulfilled)
         (> adjusted-price bid))
      (->Quote (double adjusted-price) ask)
      quote)))

(defmethod update-quote
  :sell
  [{:keys [bid ask] :as quote} {:keys [price amount]} x-amount]
  (let [adjusted-price (+ price x-amount)]
    (if (or
         (= ask :unfulfilled)
         (< adjusted-price ask))
      (->Quote bid (double adjusted-price))
      quote)))

(defn determine-quote [quote-amount x-amount]
  (fn [quote {:keys [amount] :as order}]
    (if (= quote-amount amount)
      (update-quote quote order x-amount)
      quote)))

(defn quote-or-unfulfilled
  [{:keys [bid ask] :as quote}]
  (if (or (= bid :unfulfilled)
          (= ask :unfulfilled))
    (Optional/empty)
    (Optional/of quote)))

(deftype MyRfqService [x-amount orders]
  RfqService
  (quote-for [this currency amount]
    (->
     (reduce (determine-quote amount x-amount) (->Quote :unfulfilled :unfulfilled) (orders-for currency orders))
     quote-or-unfulfilled)))



(comment

  (.orElse (quote-for (MyRfqService. 0.02M orders) "USD" 200) :unfulfilled)
  (.orElse (quote-for (MyRfqService. 0.02M orders) "USD" 300) :unfulfilled)
  (.orElse (quote-for (MyRfqService. 0.02M orders) "USD" 100) :unfulfilled)
  (.orElse (quote-for (MyRfqService. 0.02M orders) "GBP" 100) :unfulfilled)

  (.orElse (rfqservice.transduce/quote-for2 "USD" 200 0.02M) :dummy)

  (quote-for (rfqservice.transduce.TransduceRfqService. 0.02M orders) :unfulfilled)
  )
