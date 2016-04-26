(ns rfqservice.transduce
  (:gen-class)
  (:require [rfqservice.core
             :refer
             [->Quote orders quote-or-unfulfilled update-quote]]))

(defprotocol RfqService
  "Quote service"
  (quote-for [this currency amount]))

(defn determine-transducer-quote [quote-amount x-amount]
  (fn
    ([] (->Quote :unfulfilled :unfulfilled))
    ([quote] quote)
    ([quote {:keys [amount] :as order}]
     (if (= quote-amount amount)
       (update-quote quote order x-amount)
       quote))))

(deftype TransduceRfqService [x-amount orders-coll]
  RfqService
  (quote-for [this currency amount]
    (->
     (transduce (filter #(= currency (:currency %))) (determine-transducer-quote amount x-amount) orders-coll))))

(defn quote-for2 [currency amount x-amount]
    (->
     (transduce (filter #(= currency (:currency %))) (determine-transducer-quote amount x-amount) orders)
     quote-or-unfulfilled))


(comment

  (defn sum
    ([] 0)
    ([a] a)
    ([a b] (+ a b)))

  (transduce (filter odd?) sum [1 2 3 4 5 6 7 8 9 10 12 14])
  (.orElse (quote-for2 :usd 200 0.02) :unfulfilled)
  (quote-for (TransduceRfqService. 0.02 orders) :usd 200)

  )