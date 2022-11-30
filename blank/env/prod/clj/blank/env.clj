(ns blank.env
  (:require [clojure.tools.logging :as log]))

(def defaults
  {:init
   (fn []
     (log/info "\n-=[blank started successfully]=-"))
   :stop
   (fn []
     (log/info "\n-=[blank has shut down successfully]=-"))
   :middleware identity})
