(ns fun-with-manifold.demo
  (:require [manifold.deferred :as d]
            [fun-with-manifold.fruit-api :as fruit-api]))





;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;
;; Working with undelivered values using `manifold.deferred`
;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;




;;;;;;;;;;;;;;;;;;
;; `clojure.core/promise`
;;;;;;;;;;;;;;;;;;

(def p (promise))
;; => #'fun-with-manifold.demo/p

(realized? p)
;; => false

(deliver p :deliver-me!)

p

(realized? p)


;; cannot get re-delivered
(deliver p :me-too!)

p




;; callbacks
(def p (promise))

(future
  (println "1. Running on separate thread, about to wait for a promise\n")
  (println "2. Now revealing the value of promise: " (deref p))
  (println "3. Execution finished...\n\n"))

(deliver p :ta-da!)




;;; a couple of things to consider:
;;
;; - how would this affect how we model our program?
;;
;; - exceptions increasingly more awkward to handle







;;;;;;;;;;;;;;;;;;
;; `manifold.deferred`
;;;;;;;;;;;;;;;;;;

;; deliver a value
(def value-deferred (d/deferred))

(d/success! value-deferred :this-is-a-value)

(deref value-deferred)

(deliver value-deferred :something)




;; an already delivered deferred
(realized? (d/success-deferred :success-value))








;; describe computation before a value arrives
(def value-deferred (d/deferred))

(def incremented-deferred (d/chain value-deferred inc inc inc))

(d/success! value-deferred 3)

@value-deferred

@incremented-deferred







;; doesn't drastically affect program flow
(def value-deferred (d/deferred))

(def incremented-deferred
  (d/chain value-deferred (fn [v]
                            (println "I'm a Deferred!")
                            (println "My value is: " v "\n")
                            (inc v))))

(d/chain incremented-deferred (fn [v]
                                (println "My new value is: " v)))

(d/success! value-deferred 100)

@value-deferred

@incremented-deferred






;; error handling
(def value-deferred (d/deferred))

(def divide-deferred (d/chain value-deferred (partial / 1)))

(def divide-deferred-caught (d/catch divide-deferred (fn [exception]
                                                       (println "Don't divide by zero!")
                                                       100)))

(def divide-deferred-caught2 (d/chain divide-deferred-caught inc))
(d/success! value-deferred 0)

@divide-deferred-caught2








;; working with multiple deferreds
(def bananas-response (fruit-api/query-for-fruit))
(def apples-response (fruit-api/query-for-fruit))
(realized? bananas-response)
(class bananas-response)

(def total-fruit-salad-pieces
  (d/let-flow [bananas bananas-response
               apples  apples-response]
    (println "Doing hardcore fruit salad calculations...")
    (let [banana-pieces (* bananas 12)
          apple-pieces  (* apples 8)
          total-pieces  (+ banana-pieces apple-pieces)]
      (println "Finished with hardcore fruit calculations!\n")
      (println "Our result is:" total-pieces)
      total-pieces)))

(class total-fruit-salad-pieces)

;; api responds...
(d/success! bananas-response 10)
(d/error! apples-response (Exception. "No fruits!"))


@total-fruit-salad-pieces








;; clojure threading
(-> (util/web-request) ;;deferred
    (d/catch #_v1 Exception handle-web-failure)
    (d/chain #_v2 get-body)
    (d/chain #_v3 compute-result)
    (d/catch Exception handle-processing-failures)
    (d/chain clojure.pprint/pprint))

(d/chain (d/chain (d/catch (util/web-request) Exception ...) get-body))
