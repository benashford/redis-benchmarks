(ns redis-test.two
  (:require [clojure.core.async :as a]
            [redis-async.core :as redis-async]
            [redis-async.client :as client]
            [taoensso.carmine :as car]))

;; shared

(def ^:private cmds 10000)

(defn- generate-random-string []
  (str (rand-int 50000) "-" (rand-int 50000) "-" (rand-int 50000)))

(defn- generate-test-data []
  (into [] (repeatedly cmds generate-random-string)))

;; redis-async

(defn- a-generate-ids [p]
  (let [c (a/chan cmds)]
    (a/go
      (dotimes [_ cmds]
        (a/>! c (a/<! (client/incr p "ID-GEN-KEY")))))
    c))

(defn- a-populate [p test-data]
  (let [id-ch (a-generate-ids p)]
    (a/go-loop [[data & rest-of-data] test-data]
      (let [id (client/<! id-ch)]
        (client/set p (str "key:" id) data)
        (client/rpush p "QUEUE" id))
      (when rest-of-data
        (recur rest-of-data)))))

(defn test-a [p test-data]
  (client/flushdb p)
  (a/<!! (a-populate p test-data)))

;; carmine

(defmacro wcar* [& body] `(car/wcar {:pool {} :spec {}} ~@body))

(defn- b-populate [test-data]
  (let [ids       (wcar*
                   (dotimes [_ cmds]
                     (car/incr "ID-GEN-KEY")))
        test-data (map vector ids test-data)]
    (wcar*
     (loop [[[id data] & rest-of-data] test-data]
       (car/set (str "key:" id) data)
       (car/rpush "QUEUE" id)
       (when rest-of-data
         (recur rest-of-data))))))

(defn test-b [test-data]
  (wcar* (car/flushdb))
  (b-populate test-data))
