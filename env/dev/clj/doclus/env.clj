(ns doclus.env
  (:require [selmer.parser :as parser]
            [clojure.tools.logging :as log]
            [doclus.dev-middleware :refer [wrap-dev]]))

(def defaults
  {:init
   (fn []
     (parser/cache-off!)
     (log/info "\n-=[doclus started successfully using the development profile]=-"))
   :stop
   (fn []
     (log/info "\n-=[doclus has shut down successfully]=-"))
   :middleware wrap-dev})
