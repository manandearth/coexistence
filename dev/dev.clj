(ns dev
  "Tools for interactive development with the REPL. This file should
  not be included in a production build of the application.

  Call `(reset)` to reload modified code and (re)start the system.

  The system under development is `system`, referred from
  `com.stuartsierra.component.repl/system`.

  See also https://github.com/stuartsierra/component.repl"
  (:require
   [clojure.java.io :as io]
   [clojure.pprint :refer [pprint]]
   [clojure.set :as set]
   [clojure.string :as string]
   [com.grzm.component.pedestal :as pedestal-component]
   [com.stuartsierra.component :as component]
   [com.stuartsierra.component.repl :refer [set-init]]
   [modular.postgres]
   ;[background-processing.background-processor :as background-processor]
   ;[background-processing.enqueuer :as enqueuer]
   [coexistence.server]
   [coexistence.service]))

(defn dev-system
  []
  (component/system-map
   :service-map coexistence.server/dev-map
   ;:background-processor (background-processor/new :queue-name "cljtest")
   ;:enqueuer (enqueuer/new :queue-name "cljtest")
:db (modular.postgres/map->Postgres {:url "postgresql://localhost:5432/swallows" :user "swallows" :password "swallows"})
   :pedestal (component/using
              (pedestal-component/pedestal (constantly coexistence.server/dev-map))
              coexistence.service/components-to-inject)))

(set-init (fn [_]
            (dev-system)))


;;HERE for (reset) to work, I need to `C-c C-k` this ns .

(defn refresh []
  (clojure.core/when-let [v (do
                            (clojure.core/require 'clojure.tools.namespace.repl)
                            ((clojure.core/resolve 'clojure.tools.namespace.repl/set-refresh-dirs) "src" "test")
                            ((clojure.core/resolve 'clojure.tools.namespace.repl/refresh)))]
  (clojure.core/when (clojure.core/instance? java.lang.Throwable v)
    (clojure.core/when (clojure.core/instance? java.io.FileNotFoundException v)
      ((clojure.core/resolve 'clojure.tools.namespace.repl/clear)))
    (throw v))))

(defn reset []
  (do
  (clojure.core/require 'clojure.tools.namespace.repl)
  (clojure.core/require 'com.stuartsierra.component.repl)
  ((clojure.core/resolve 'clojure.tools.namespace.repl/set-refresh-dirs) "src" "test")
  (try
    ((clojure.core/resolve 'com.stuartsierra.component.repl/reset))
    (catch java.lang.Throwable v
      (clojure.core/when (clojure.core/instance? java.io.FileNotFoundException v)
        ((clojure.core/resolve 'clojure.tools.namespace.repl/clear)))
      (clojure.core/when ((clojure.core/resolve 'com.stuartsierra.component/ex-component?) v)
        (clojure.core/let [stop (clojure.core/resolve 'com.stuartsierra.component/stop)]
          (clojure.core/some-> v clojure.core/ex-data :system stop)))
      (throw v))))
)
