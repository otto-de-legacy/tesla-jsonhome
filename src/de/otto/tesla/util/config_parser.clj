(ns de.otto.tesla.util.config-parser)

(defn deep-merge
  "Recursively merges maps. If keys are not maps, the last value wins."
  [& vals]
  (if (every? map? vals)
    (apply merge-with deep-merge vals)
    (last vals)))

(declare nest-single-tuple)

(defn prop->nested-hash [prop-hash]
  (apply deep-merge (map nest-single-tuple prop-hash)))

(defn nest-single-tuple [[key value]]
  (let [[head tail] (clojure.string/split (name key) #"-" 2)
        new-value (if tail
                    (prop->nested-hash {(keyword tail) value})
                    value)]
    {(keyword head) new-value}))