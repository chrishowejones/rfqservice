(ns rfqservice.performance-test
  (:require [rfqservice.core :refer [orders quote-for]]
            [rfqservice.reducer]
            [rfqservice.transduce]
            [clojure.core.reducers :as r])
  (:import rfqservice.core.MyRfqService
           rfqservice.reducer.ReducerRfqService
           rfqservice.transduce.TransduceRfqService))

;; Interesting performance test results
;; All three implementations of the RfqService performed similarly
;; I expected the reducer version to use parallelisation and therefore
;; out perform the others but it performed the same or fractionally worse than the transducer version.
;; So unless something in my implementation is preventing parallelisation I can only assume that the
;; filter (which is the only bit that can be parallelised) is so quick it little difference and the
;; overhead of fork/join eats up that gain and more.

(defn test-orders []
  (reduce concat (repeat 1000 orders)))

(let [my-service (MyRfqService. 0.02M (test-orders))]
  (println "MyRfqService Performance")
  (time
   (dotimes [x 1000]
     (quote-for my-service "USD" 200))))

(let [my-service (TransduceRfqService. 0.02M (test-orders))]
  (println "TransduceRfqService Performance")
  (time
   (dotimes [x 1000]
     (quote-for my-service "USD" 200))))

(let [my-service (ReducerRfqService. 0.02M (test-orders))]
  (println "ReducerRfqService Performance")
  (time
   (dotimes [x 1000]
     (quote-for my-service "USD" 200))))


(defn benchmark [f N times]
  (let [nums (vec (range N))
        start (java.lang.System/currentTimeMillis)]
    (dotimes [n times]
      (f nums))
    (- (java.lang.System/currentTimeMillis) start)))

(defn eager-map
  "A dumb map"
  [& args]
  (doall (apply map args)))

(defn eager-filter
  "An eager filter"
  [& args]
  (doall (apply filter args)))

(defn eager-test [nums]
  (eager-filter even? (eager-map inc nums)))

(defn lazy-test [nums]
  (doall (filter even? (map inc nums))))

(defn reducer-test [nums]
  (into [] (r/filter even? (r/map inc nums))))

(println "Eager v. Lazy filter+map, N=1000000, 10 repetitions")
(println "Eager test: " (benchmark eager-test 1000000 10) "ms")
(println "Lazy test:  " (benchmark lazy-test 1000000 10) "ms")
(println "Reducers test: " (benchmark reducer-test 1000000 10) "ms")

(defn old-reduce [nums]
  (reduce + (map inc (map inc (map inc nums)))))

(defn new-reduce [nums]
  (reduce + (r/map inc (r/map inc (r/map inc nums)))))

(defn new-fold [nums]
  (r/fold + (r/map inc (r/map inc (r/map inc nums)))))

(def N 1000000)
(def times 10)
(println "Old reduce: " (benchmark old-reduce N times) "ms")
(println "New reduce: " (benchmark new-reduce N times) "ms")
(println "New fold:   " (benchmark new-fold N times) "ms")
