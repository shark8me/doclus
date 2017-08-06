(ns user
  (:require [mount.core :as mount]
            doclus.core))

(defn start []
  (mount/start-without #'doclus.core/repl-server))

(defn stop []
  (mount/stop-except #'doclus.core/repl-server))

(defn restart []
  (stop)
  (start))


