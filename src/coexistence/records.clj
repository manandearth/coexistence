(ns coexistence.records
  (:require
   [clj-postgresql.core :as pg]
   [clojure.data.json :as json]
   [clojure.java.jdbc :as jdbc]
   [honeysql.core :as sql]
   [honeysql.helpers :as helpers :refer :all]
   [java-time :refer :all
    ;; :exclude [update contains? iterate range min max zero?]
    ]
   [clojure.string :as string]))

(def today (sql-date (local-date)))

;; (java-time.convert  )
;; (clojure.java.jdbc)
;; (clj-time.jdbc )

(def conn {:dbtype "postgresql"
           :dbname "swallows"
           :host "localhost"
           :user "swallows"
           :password "swallows"})

(def dbspec "postgresql://swallows:swallows@localhost:5432/swallows")

(defn extract-uri [db]
  (let [p db
        p-vec (clojure.string/split (:url p)  #"/")]
    (str (first p-vec) "//" (get p-vec 3) ":" (get p-vec 3) "@" (get p-vec 2) "/" (get p-vec 3) )))

(defn comp-db-q [db]
  (let [uri (extract-uri db)]
    (jdbc/query uri (sql/format {:select [:*]
                                 :from [:nests]
                                 :where [:= :species "penguin"]
                                 }))))


(defn create-nest-table []
  (jdbc/db-do-commands conn
                       (jdbc/create-table-ddl
                        :nests
                        [[:id "serial PRIMARY KEY"]
                         [:street "varchar(64)"]
                         [:number "int"]
                         [:gps "point"] ;-> (pg/point <float><float>)
                         [:species "varchar(64)"]
                         [:height "int"]
                         [:facing "varchar(32)"]
                         [:type "varchar(100)"]
                         [:date "date"]
                         [:destroyed "boolean"]
                         [:destroyed_date "date"]])))

;;TODO check that all the types above are correct and re create the table.
;; (create-nest-table)




(defn add-nest [entry]
  (jdbc/insert! conn :nests entry))

;;first method attempt:
(defn insert-nest!
  [entry-map]
  (let [entry {:street (:street entry-map)
               :number (:number entry-map)
               :gps (:gps entry-map)
               :species (:species entry-map)
               :height (:height entry-map)
               :facing (:facing entry-map)
               :type (:type-of entry-map)
               :date (:date entry-map)
               :destroyed (:destroyed entry-map)
               :destroyed_date (:destroyed_date entry-map)}]
    (jdbc/insert! conn :nests entry)))

(defn insert-nest2!
  [entry-map db]
  (let [entry {:street (:street entry-map)
               :number (:number entry-map)
               :gps (:gps entry-map)
               :species (:species entry-map)
               :height (:height entry-map)
               :facing (:facing entry-map)
               :type (:type-of entry-map)
               :date (:date entry-map)
               :destroyed (:destroyed entry-map)
               :destroyed_date (:destroyed_date entry-map)}]
    (jdbc/insert! (extract-uri db) :nests entry)))

;;HONEYSQL


(def sqlmap {:select [:id :street :number]
             :from [:nests]
             :where [:> :id 1]})

;(sql/format sqlmap)

;(jdbc/query conn (sql/format sqlmap))

;; (insert-nest! {:street "Doop" :species "unicorn"})
;; (count (jdbc/query conn ["SELECT * FROM nests WHERE (species = 'unicorn')"]))

;(-> sqlmap (select :*))


(def test-map (-> (insert-into :nests)
                  (columns :street :number :gps :species :height :facing :type :date :destroyed :destroyed-date)
                  (values
                   [["Altozano"  1  (pg/point 33.142 0.221) "swallow" 6 "NW" "in window" today false nil]
                    ["Altozano"  1  (pg/point 33.142 0.221) "swallow" 6 "NW" "in window" today false nil]
                    ["Altozano"  1  (pg/point 33.142 0.221) "swallow" 6 "NW" "in window" today false nil]
                    ["Altozano" 4  (pg/point 33.122 0.220) "swift" 7 "NW" "under porch" today false nil]
                    ["Altozano" 3  (pg/point 33.311 0.224) "swallow" 6 "NW" "in pergola" today false nil]
                    ["Altozano" 3  (pg/point 33.311 0.224) "swallow" 6 "NW" "in pergola" today false nil]
                    ["Altozano" 3  (pg/point 33.311 0.224) "swallow" 6 "NW" "in pergola" today false nil]
                    ["Altozano" 3  (pg/point 33.311 0.224) "swallow" 6 "NW" "in pergola" today false nil]
                    ["Altozano" 9  (pg/point 33.122 0.326) "swift" 12 "N" "under porch" today false nil]
                    ["Altozano" 2  (pg/point 33.222 0.211) "swallow" 6 "NW" "in pergola" today false nil]
                    ["Altozano" 2  (pg/point 33.222 0.211) "swallow" 6 "NW" "in pergola" today false nil]
                    ["Mereced" 2 (pg/point 33.554 0.301) "swallow" 5 "NW" "under roof" today false nil]
                    ["Mereced" 4 (pg/point 33.553 0.300) "swallow" 6 "N"  "under roof" today false nil]
                    ["Mereced" 4 (pg/point 33.553 0.300) "swallow" 6 "N"  "under roof" today false nil]
                    ["Mereced" 4 (pg/point 33.553 0.300) "swallow" 6 "N"  "under roof" today false nil]
                    ["Mereced" 4 (pg/point 33.553 0.300) "swallow" 6 "N"  "under roof" today false nil]
                    ["Mereced" 4 (pg/point 33.553 0.300) "swallow" 6 "N"  "under roof" today false nil]
                    ["Don Salvador" 2 (pg/point 31.262 0.265) "swallow" 8 "NE" "corner porch" today false nil]
                    ["Don Salvador" 2 (pg/point 31.262 0.265) "swallow" 8 "NE" "corner porch" today false nil]
                    ["Don Salvador" 2 (pg/point 31.262 0.265) "swallow" 8 "NE" "corner porch" today false nil]
                    ["Don Salvador" 1 (pg/point 31.272 0.266) "swift" 9 "NW" "in window" today false nil]
                    ["Don Salvador" 1 (pg/point 31.272 0.266) "swift" 9 "NW" "in window" today false nil]
                    ["Don Salvador" 1 (pg/point 31.272 0.266) "swift" 9 "NW" "in window" today false nil]
                    ["Plazuela" 5 (pg/point 33.678 0.216) "swallow" 9 "NW" "over window" today false nil]
                    ["Plazuela" 5 (pg/point 33.678 0.216) "swallow" 9 "NW" "over window" today false nil]
                    ["Plazuela" 5 (pg/point 33.678 0.216) "swallow" 9 "NW" "over window" today false nil]
                    ["Plazuela" 5 (pg/point 33.678 0.216) "swallow" 9 "NW" "over window" today false nil]
                    ["Plazuela" 2 (pg/point 33.676 0.213) "swallow" 8 "NW" "under roof" today false nil]
                    ["Plazuela" 2 (pg/point 33.676 0.213) "swallow" 8 "NW" "under roof" today false nil]
                    ["Plazuela" 2 (pg/point 33.676 0.213) "swallow" 8 "NW" "under roof" today false nil]
                    ["Rosario" 6 (pg/point 33.677 0.214) "swift" 6 "N" "under pergola" today false nil]
                    ["Rosario" 6 (pg/point 33.677 0.214) "swift" 6 "N" "under pergola" today false nil]
                    ["Rosario" 6 (pg/point 33.677 0.214) "swift" 6 "N" "under pergola" today false nil]
                    ["Rosario" 3 (pg/point 33.679 0.219) "swift" 4 "N" "under pergola" today false nil]
                    ["Rosario" 1 (pg/point 33.676 0.218) "swallow" 6 "NE" "in porch" today false nil]
                    ["Rosario" 1 (pg/point 33.676 0.218) "swallow" 6 "NE" "in porch" today false nil]
                    ["Rosario" 1 (pg/point 33.676 0.218) "swallow" 6 "NE" "in porch" today false nil]
                    ["Rosario" 2 (pg/point 33.654 0.217) "swallow" 7 "NW" "in window" today false nil]
                    ["Juan Bueno" 4 (pg/point 32.454 0.210) "swift" 6 "NW" "under roof" today false nil]
                    ["Juan Bueno" 4 (pg/point 32.454 0.210) "swift" 6 "NW" "under roof" today false nil]
                    ["Juan Bueno" 4 (pg/point 32.454 0.210) "swift" 6 "NW" "under roof" today false nil]
                    ["Juan Bueno" 5 (pg/point 32.565 0.266) "swift" 6 "NW" "under roof" today false nil]
                    ["Juan Bueno" 3 (pg/point 32.566 0.222) "swift" 5 "N" "under porch" today false nil]
                    ["Juan Bueno" 3 (pg/point 32.566 0.222) "swift" 5 "N" "under porch" today false nil]
                    ["Juan Bueno" 3 (pg/point 32.566 0.222) "swift" 5 "N" "under porch" today false nil]
                    ["Juan Bueno" 1 (pg/point 32.677 0.201) "swallow" 5 "N" "in window" today false nil]
                    ["Juan Bueno" 1 (pg/point 32.677 0.201) "swallow" 5 "N" "in window" today false nil]])
                  sql/format))


;; (jdbc/with-db-transaction [db conn] ;makes sure that the transaction is carried only if there are no errors, cool!
;;                       (jdbc/execute!
;;                        db
;;                        test-map))


(def select-all-query
  (jdbc/query conn (sql/format  {:select [:*]
                                 :from [:nests]})))


;; (comp-db-q dbspec)
;; (comp-db-q {:dbname "swallows"
;;             :url "jdbc:postgresql://swallows@localhost:5432"
;;             :user "swallows"
;;             :password "swallows"})


(defn insert-entry [address]
  (let [results
        (jdbc/execute! conn
                      (-> (insert-into :nests)
                          (columns :address)
                          (values [[address]])
                          sql/format)
                      {:return-keys ["id" "address"]})]
    (assert (= (count results) 2))
    results))


