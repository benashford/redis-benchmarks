(ns redis-test.core
  (:require [redis-async.core :as redis-async]
            [redis-async.client :as client]
            [taoensso.carmine :as car]))

(def ^:private cmds 1024)

(defn test-redis-async [p]
  (let [cs (mapv (fn [_] (client/ping p)) (range cmds))]
    (mapv #(client/<!! %) cs)))

(defmacro wcar* [& body] `(car/wcar {:pool {} :spec {}} ~@body))

(defn test-carmine-pipe []
  (wcar* (dotimes [_ cmds]
           (car/ping))))

(defn test-carmine []
  (mapv (fn [_] (car/wcar {:pool {} :spec {}} (car/ping))) (range cmds)))
