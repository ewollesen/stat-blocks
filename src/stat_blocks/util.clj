(ns stat-blocks.util
  (:require [clojure.string :as str]))


(def die-roll-re #"(\d+)?d(\d+)(?:\s*(\+|-)\s*(\d+))?")
(def format-mod (partial format "%+d"))
(def range-re #"([0-9]+)/([0-9]+)")
(def reach-or-range-re #"([0-9]+)/([0-9]+/[0-9]+/)?")
(def str->int (comp #(Integer. %) str/trim str))


(defn calc-ability-mod [score]
  (int (/ (- score 10) 2)))

(defn join-sentence [pieces]
  (let [head (butlast pieces)
        tail (last pieces)]
    (condp = (count pieces)
      0 ""
      1 (first pieces)
      2 (str/join " and " pieces)
      (str/join ", and " [(str/join ", " head) tail]))))

(defn mean-roll [text]
  (let [[_ rolls sides sign mod] (re-find die-roll-re text)
        op (if (= "-" sign) - +)
        avg (+ (quot (Integer. (or sides 0)) 2) 1/2)]
    (quot (op (* (Integer. (or rolls 1)) avg) (Integer. (or mod 0))) 1)))
