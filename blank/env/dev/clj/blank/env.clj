(ns blank.env
  (:require
    [selmer.parser :as parser]
    [clojure.tools.logging :as log]
    [blank.dev-middleware :refer [wrap-dev]]))

(def defaults
  {:init
   (fn []
     (parser/cache-off!)
     (log/info "\n-=[blank started successfully using the development profile]=-"))
   :stop
   (fn []
     (log/info "\n-=[blank has shut down successfully]=-"))
   :middleware wrap-dev})
