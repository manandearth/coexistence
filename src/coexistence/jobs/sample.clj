(ns coexistence.jobs.sample)

;; (defrecord Sample [temperature]
;;   background-job/BackgroundJob
;;   (perform [_]
;;     (log/error ::temperature (* 100 temperature)))
;;   (type [_]
;;     ::background-job/cpu-bound))

;; (defn new [temperature]
;;   (map->Sample {:temperature temperature}))
