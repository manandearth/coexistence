(ns coexistence.service
  (:require [clojure.core.async :as async :refer [go >!]]
            [clojure.spec.alpha :as spec]
            [com.grzm.component.pedestal :as pedestal-component]
            [io.pedestal.http :as http]
            [io.pedestal.http.body-params :as body-params]
            [io.pedestal.http.route :as route]
            [io.pedestal.interceptor.chain :as interceptor-chain]
            [coexistence.coerce :as coerce]
            [ring.util.response :as ring-resp]
            ;;ADDED ONES:
            [clojure.data.json :as json]
            [io.pedestal.http.content-negotiation :as conneg]
            [io.pedestal.test :as test]
            [coexistence.records :as records] ;LOOK HOW TO REMOVE
            [coexistence.views :as views]
            ))

;; (defn about-page
;;   [request]
;;   (ring-resp/response (format "Clojure %s - served from %s"
;;                               (clojure-version)
;;                               ;; (route/url-for ::about-page)
;;                               )))

;; (defn about-page [request]
;;   (->> (route/url-for ::about-page)
;;        (format "Clojure %s - served from %s"
;;                (clojure-version))
;;        ring-resp/response))



(defn extracting-test [{{:keys [temperature orientation]} :query-params :keys [db] :as request}]
  (str request)
  )

;; (extracting-test [12 :east])


(defn nests-page
  [request]
  (ring-resp/response (views/list-of-entries)))

(defn insert-entry-page
  [request]
  (ring-resp/response (views/insert-to-db)))

(defn insert-entry-page2
  [request]
  (ring-resp/response (views/insert-to-db2)))

;; (defn insert-entry-result
;;   [request]
;;   (ring-resp/response (views/insert-to-db-results)))

(defn return-params-page
  [request]
  (ring-resp/response (views/insert-to-db-results request)))

(defn return-params-page2
  [{{:keys [db]} :query-params :keys [db params] :as request}]
  (ring-resp/response
   (views/insert-to-db-results2
    {:db db :params params}))
  )

(defn home-page
  [request]
  (ring-resp/response (views/home)))

(defn test-home-page
  [request]
  (ring-resp/response (views/home)))

(defn about-page
  [request]
  (ring-resp/response (views/about)))


(defn comp-query
  [{:keys [db] :as context}]
  (let [url (get-in context [:db :url])
        user (get-in context [:db :user])
        password (get-in context [:db :password])
        temp-url {}]
    (ring-resp/response  
    (records/comp-db-q {:url url :user user :password password})
    )
  ))

;; FROM SWALLOWS:
(def supported-types ["text/html" "application/edn" "application/json" "text/plain"])

(def content-neg-intc (conneg/negotiate-content supported-types))

(defn accepted-type
  [context]
  (get-in context [:request :accept :field] "text/plain"))

(defn transform-content
  [body content-type]
  (case content-type
    "text/html"  body
    "text/plain"       body
    c"application/edn"  (pr-str body)
    "application/json" (json/write-str body)))


(defn coerce-to
  [response content-type]
  (-> response
      (update :body transform-content content-type)
      (assoc-in [:headers "Content-Type"] content-type)))

(def coerce-body
  {:name ::coerce-body
   :leave
   (fn [context]
     (cond-> context
       (nil? (get-in context [:response :headers "Content-Type"]))
       (update-in [:response] coerce-to (accepted-type context))))})



;FROM VEMV

(spec/def ::temperature int?)

(spec/def ::orientation (spec/and keyword? #{:north :south :east :west}))

(spec/def ::api (spec/keys :req-un [::temperature ::orientation]))

;; (defn api [{{:keys [temperature orientation]} :query-params :keys [db enqueuer] :as request}]
;;   (go
;;     (-> enqueuer :channel (>! (coexistence.jobs.sample/new temperature))))
;;   {:status 200
;;    :body   {:temperature temperature :orientation orientation}})


(defn param-spec-interceptor
  "Coerces params according to a spec. If invalid, aborts the interceptor-chain with 422, explaining the issue."
  [spec params-key]
  {:name  ::param-spec-interceptor
   :enter (fn [context]
            (let [result (coerce/coerce-map-indicating-invalidity spec (get-in context [:request params-key]))]
              (if (contains? result ::coerce/invalid?)
                (-> context
                    (assoc :response {:status 422
                                      :body   {:explanation (spec/explain-str spec result)}})
                    interceptor-chain/terminate)
                (assoc-in context [:request params-key] result))))})

(defn context-injector [components]
  {:enter (fn [{:keys [request] :as context}]
            (reduce (fn [v component]
                      (assoc-in v [:request component] (pedestal-component/use-component request component)))
                    context
                    components))
   :name  ::context-injector})

(def components-to-inject [:db
 ;:background-processor :enqueuer
                           ])

(def component-interceptors
  (conj (mapv pedestal-component/using-component components-to-inject)
        (context-injector components-to-inject)))

(def common-interceptors (into component-interceptors [(body-params/body-params) http/html-body]))

(def routes
  "Tabular routes"
  #{["/" :get  (conj common-interceptors `home-page)]
    ["/about" :get (conj common-interceptors `about-page)]
    ["/nests" :get (conj common-interceptors coerce-body content-neg-intc `nests-page)]
    ["/add-address" :get (conj component-interceptors coerce-body content-neg-intc `insert-entry-page)]
    ["/add-address" :post  (conj component-interceptors coerce-body content-neg-intc `return-params-page)]
    ["/comp-query" :get (conj component-interceptors coerce-body content-neg-intc `comp-query)]
    ["/add-address2" :get (conj common-interceptors coerce-body content-neg-intc `insert-entry-page2)]
    ["/add-address2" :post  (conj common-interceptors coerce-body content-neg-intc `return-params-page2)]
    ;; ["/api" :get (into component-interceptors [http/json-body (param-spec-interceptor ::api :query-params) `api])]
    ;["/invoices/insert" :get (into component-interceptors [http/json-body (param-spec-interceptor ::invoices.insert/api :query-params) `invoices.insert/perform])]
    ;["/invoices/:id" :get (into component-interceptors [http/json-body (param-spec-interceptor ::invoices.retrieve/api :path-params) `invoices.retrieve/perform])]
    ;["/invoices/delete" :get (into component-interceptors [http/json-body `invoices.delete/perform])]
    })

(comment
  (def routes
    "Map-based routes"
    `{"/" {:interceptors [(body-params/body-params) http/html-body]
           :get          home-page
           "/about"      {:get about-page}}})
  (def routes
    "Terse/Vector-based routes"
    `[[["/" {:get home-page}
        ^:interceptors [(body-params/body-params) http/html-body]
        ["/about" {:get about-page}]]]]))

;; Consumed by coexistence.server/create-server
;; See http/default-interceptors for additional options you can configure
(def service
  {:env                     :prod
   ;; You can bring your own non-default interceptors. Make
   ;; sure you include routing and set it up right for
   ;; dev-mode. If you do, many other keys for configuring
   ;; default interceptors will be ignored.
   ;; ::http/interceptors []
   ::http/routes            routes
   ;; Uncomment next line to enable CORS support, add
   ;; string(s) specifying scheme, host and port for
   ;; allowed source(s):
   ;;
   ;; "http://localhost:8080"
   ;;
   ;; ::http/allowed-origins ["scheme://host:port"]

   ;; Tune the Secure Headers
   ;; and specifically the Content Security Policy appropriate to your service/application
   ;; For more information, see: https://content-security-policy.com/
   ;;   See also: https://github.com/pedestal/pedestal/issues/499
   ;; ::http/secure-headers {:content-security-policy-settings {:object-src "'none'"
   ;; :script-src "'unsafe-inline' 'unsafe-eval' 'strict-dynamic' https: http:"
   ;; :frame-ancestors "'none'"}}

   ;; Root for resource interceptor that is available by default.
   ::http/resource-path     "/public"

   ;; Either :jetty, :immutant or :tomcat (see comments in project.clj)
   ;; This can also be your own chain provider/server-fn -- http://pedestal.io/reference/architecture-overview#_chain_provider
   ::http/type              :jetty
   ;; ::http/host "localhost"
   ::http/port              8080
   ;; Options to pass to the container (Jetty)
   ::http/container-options {:h2c? true
                             :h2?  false
                             ;; :keystore "test/hp/keystore.jks"
                             ;; :key-password "password"
                             ;; :ssl-port 8443
                             :ssl? false}})
