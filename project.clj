(defproject coexistence "0.0.1-SNAPSHOT"
  :description ""
  :url ""
  :license {:name "Eclipse Public License"
            :url "http://www.eclipse.org/legal/epl-v10.html"}
  :dependencies [;[amazonica "0.3.125"]
                 ;[background-processing "0.1.0-SNAPSHOT"]
                 [better-cond "1.0.1"]
                 [ch.qos.logback/logback-classic "1.1.8" :exclusions [org.slf4j/slf4j-api]]
                 [com.gfredericks/how-to-ns "0.1.9"]
                 [com.grzm/component.pedestal "0.1.7"]
                 [com.mchange/c3p0 "0.9.5.2"]
                 [com.stuartsierra/component "0.3.2"]
                 [com.stuartsierra/component.repl "0.2.0"]
                 [org.clojure/tools.namespace "0.2.11"]
                 [org.clojure/tools.nrepl "0.2.13"]
                 [expound "0.6.0"]
                 [honeysql "0.9.4"]
                 ;; [org.postgres␘oql/postgresql "42.2.5.jre7"]
                 [clj-postgresql "0.7.0"]
                 [clojure.java-time "0.3.2"]
                 [hiccup "1.0.5"]
                 [hiccup-table "0.2.0"]
                 ;; [formatting-stack "0.10.2"]
                 [io.pedestal/pedestal.jetty "0.5.3"]
                 [io.pedestal/pedestal.service "0.5.3"]
                 [juxt.modular/postgres "0.0.1-SNAPSHOT"]
                 [org.clojure/clojure "1.9.0"]
                 [org.clojure/core.async "0.4.474"]
                 [org.clojure/java.jdbc "0.7.6"]
                 [org.clojure/tools.logging "0.4.0"]
                 [org.slf4j/jcl-over-slf4j "1.7.22"]
                 [org.slf4j/jul-to-slf4j "1.7.22"]
                 [org.slf4j/log4j-over-slf4j "1.7.22"]
                 [prismatic/schema "1.1.9"]
                 [spec-coerce "1.0.0-alpha6"]
                 [org.clojure/data.json "0.2.6"]]
  :repl-options {:port 41234}
  :min-lein-version "2.0.0"
  :resource-paths ["config" "resources"]
  :profiles {:dev {:dependencies [[io.pedestal/pedestal.service-tools "0.5.3"]]
                   :source-paths ["dev"]}
             :uberjar {:aot [coexistence.server]}}
  :main ^{:skip-aot true} coexistence.server)
