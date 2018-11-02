(ns fun-with-manifold.demo
  (:require [manifold.deferred :as d]))

;;;;;;;;;;;;;;;;;;
;; `clojure.core/promise`
;;;;;;;;;;;;;;;;;;

(def p (promise))

(realized? p)

(deliver p :deliver-me!)
;; => nil

p

(realized? p)


;; cannot get re-delivered
(deliver p :me-too!)

p




;; callbacks
(def p (promise))

(future
  (println "running on separate thread, about to wait for a promise\n")
  (println "now revealing the value of promise: " (deref p))
  (println "execution finished...\n\n"))

(deliver p :ta-da!)




;;; a couple of things to consider:
;;
;; - how would this affect how we model our program?
;;
;; - exceptions increasingly more awkward to handle







;;;;;;;;;;;;;;;;;;
;; `manifold.deferred`
;;;;;;;;;;;;;;;;;;

(def v (d/deferred))

v

;; an already delivered deferred
(realized? (d/success-deferred :this-is-a-value))




;; deliver a value
(def v (d/deferred))

(deliver v 1)
(d/success! v 1)

@v



;; describe computation before a value arrives
(def d (d/deferred))

(def inc-d (d/chain d inc))

(d/success! d 3)

d

@inc-d




;; doesn't drastically affect program flow
(def d (d/deferred))

(def d2
  (d/chain d (fn [v]
               (println "I'm a Deferred!")
               (println "My value is: " v "\n")
               (inc v))))

(d/success! d 100)
@d
@d2


;; error handling
(def d (d/deferred))

(def d2 (d/chain d (partial / 1)))

(def d3-catcher (d/catch d2 (fn [exception]
                              (println "Don't divide by zero!")
                              exception)))

(d/success! d 0)

@d3-catcher





;; working with multiple deferreds
(def bananas-query (d/deferred))
(def apples-query (d/deferred))

(def total-fruit-salad-pieces
  (d/let-flow [bananas bananas-query
               apples  apples-query]
    (println "Doing hardcore fruit calculations...")
    (let [banana-pieces (* bananas 12)
          apple-pieces  (* apples 8)
          total-pieces  (+ banana-pieces apple-pieces)]
      (println "Finished with hardcore fruit calculations!\n")
      total-pieces)))

;; database responds...
(d/success! bananas-query 10)
(d/success! apples-query 10)

(future (println @total-fruit-salad-pieces))






(defn get-body [v] (:body v))

;; clojure threading
(-> (util/web-request) ;;deferred
    (d/catch #_d1 Exception handle-web-failure)
    (d/chain #_d2 get-body)
    (d/chain compute-result)
    (d/catch Exception handle-processing-failures)
    clojure.pprint/pprint)
