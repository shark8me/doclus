(ns doclus.env
  (:require [clojure.tools.logging :as log]))

(def defaults
  {:init
   (fn []
     (log/info "\n-=[doclus started successfully]=-"))
   :stop
   (fn []
     (log/info "\n-=[doclus has shut down successfully]=-"))
   :middleware identity})
