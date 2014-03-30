(ns ^:skip-wiki clojure.core.typed.chk.common.mm-env
  (:require [clojure.core.typed.chk.common.utils :as u]
            [clojure.core.typed.rt.common.current-impl :as impl]
            [clojure.core.typed.chk.common.type-rep :as r]
            [clojure.core.typed.chk.common.parse-unparse :as prs]))

(alter-meta! *ns* assoc :skip-wiki true)

;; Environment for storing multimethod types and inferred filters

(def initial-mm-dispatch-env {})

; (Atom (Seqable (IPersistentMap Symbol '{:fn-type Type, :dispatch-result (U nil Type)})))
(defonce MULTIMETHOD-DISPATCH-ENV (atom initial-mm-dispatch-env
                                        :validator (u/hash-c?
                                                     (every-pred symbol? namespace)
                                                     r/Type?)))

(defn reset-mm-dispatch-env! []
  (reset! MULTIMETHOD-DISPATCH-ENV initial-mm-dispatch-env)
  nil)

; [Symbol Filter -> nil]
(defn add-multimethod-dispatch-type
  "Add the type of the dispatch function of the multimethod named by mmsym
  to the environment. If already exists, must be identical."
  [mmsym dtype]
  {:pre [(symbol? mmsym)
         (r/Type? dtype)]}
  (impl/assert-clojure)
  (when-let [old (@MULTIMETHOD-DISPATCH-ENV mmsym)]
    (assert (= old dtype)
            (str "Inconsistent dispatch type inferred for multimethod: " mmsym
                 ".  JVM process restart probably necessary.")))
  (swap! MULTIMETHOD-DISPATCH-ENV assoc mmsym dtype)
  nil)

(defn multimethod-dispatch-type 
  "Can return nil"
  [mmsym]
  {:pre [(symbol? mmsym)]
   :post [((some-fn nil? r/Type?) %)]}
  (impl/assert-clojure)
  (@MULTIMETHOD-DISPATCH-ENV mmsym))

(defn get-multimethod-dispatch-type [mmsym]
  {:pre [(symbol? mmsym)]
   :post [(r/Type? %)]}
  (impl/assert-clojure)
  (let [t (@MULTIMETHOD-DISPATCH-ENV mmsym)]
    (assert t (str "Multimethod requires dispatch type: " mmsym))
    t))
