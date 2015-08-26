(ns redis-test.core
  (:require [redis-async.core :as redis-async]
            [redis-async.client :as client]
            [taoensso.carmine :as car]))

(def ^:private cmds 1000)

(defn test-redis-async [p]
  (let [last-c (last (repeatedly cmds #(client/ping p)))]
    (client/<!! last-c)))

(defmacro wcar* [& body] `(car/wcar {:pool {} :spec {}} ~@body))

(defn test-carmine-pipe []
  (last (wcar* (dotimes [i cmds]
                 (car/ping)))))

(defn test-carmine []
  (last (map (fn [_] (car/wcar {:pool {} :spec {}} (car/ping))) (range cmds))))
