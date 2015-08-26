(ns redis-test.core
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

(defn- a-populate [p test-data]
  (a/go-loop [[data & rest-of-data] test-data
              id-c                  (client/incr p "ID-GEN-KEY")]
    (let [id (client/<! id-c)]
      (client/set p (str "key:" id) data)
      (client/rpush p "QUEUE" id))
    (when rest-of-data
      (recur rest-of-data (client/incr p "ID-GEN-KEY")))))

(defn test-a [p test-data]
  (client/flushdb p)
  (a/<!! (a-populate p test-data)))

;; carmine

(defmacro wcar* [& body] `(car/wcar {:pool {} :spec {}} ~@body))

(defn- b-populate [test-data]
  (loop [[data & rest-of-data] test-data
         id                    (wcar* (car/incr "ID-GEN-KEY"))]
    (let [[_ _ next-id] (wcar*
                         (car/set (str "key:" id) data)
                         (car/rpush "QUEUE" id)
                         (car/incr "ID-GEN-KEY"))]
      (when rest-of-data
        (recur rest-of-data next-id)))))

(defn test-b [test-data]
  (wcar* (car/flushdb))
  (b-populate test-data))
