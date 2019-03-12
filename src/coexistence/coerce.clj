(ns coexistence.coerce
  (:require [clojure.spec.alpha :as spec]
            [coexistence.spec-utils :refer [check!]]
            [spec-coerce.core :as coerce]))

(defn coerce
  ([v]
   (coerce/coerce-structure v))
  ([spec v]
   (coerce/coerce spec v)))

(defn coerce!
  [spec v]
  (let [v (coerce/coerce spec v)]
    (check! spec v)
    v))

(defn coerce-map-indicating-invalidity [spec v]
  (let [v (coerce/coerce spec v)]
    (if (spec/valid? spec v)
      v
      (assoc v ::invalid? true))))
