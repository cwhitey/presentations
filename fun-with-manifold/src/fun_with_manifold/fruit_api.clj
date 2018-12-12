(ns fun-with-manifold.fruit-api
  (:require [manifold.deferred :as d]))

(def query-for-fruit (fn [& args] (d/deferred)))
