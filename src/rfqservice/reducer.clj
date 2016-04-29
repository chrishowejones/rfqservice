(ns rfqservice.reducer
  (:require [clojure.core.reducers :as r]
            [rfqservice.core :refer [update-quote quote-for quote-or-unfulfilled ->Quote RfqService]]))

(defn determine-reducer-quote [quote-amount x-amount]
  (fn
    ([] (->Quote :unfulfilled :unfulfilled))
    ([quote {:keys [amount] :as order}]
     (if (= quote-amount amount)
       (update-quote quote order x-amount)
       quote))))

(deftype ReducerRfqService [x-amount orders-coll]
  RfqService
  (quote-for [this currency amount]
    (quote-or-unfulfilled
     (r/fold (determine-reducer-quote amount x-amount) (r/filter #(= currency (:currency %))
                                                                 orders-coll)))))


(comment

  (quote-for (ReducerRfqService. 0.02M rfqservice.core/orders) "USD" 200)
  (let [reducing-fn (determine-reducer-quote 200 0.02M)]
    (r/fold reducing-fn orders))


  )
