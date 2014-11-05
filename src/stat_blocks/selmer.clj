(ns stat-blocks.selmer
  (:require [clojure.java.io :as io]
            [clojure.string :as str]

            [selmer.parser :as sp]
            [selmer.filters :as sf]

            [stat-blocks.util :as util]))


(def options {:tag-open \< :tag-close \>})
(def template "template.tex.selmer")


(defn format-mod [text]
  (-> text
      util/str->int
      util/format-mod))

(defn calc-ability-mod [text]
  (-> text
      util/str->int
      util/calc-ability-mod
      util/format-mod))

(defn load-template []
  (-> template
      io/resource
      io/file
      slurp))

(defn merge-context-utils [context]
  (merge context {:actions (map #(merge % {:reach-and-range "reach ? ft."}) (:actions context))}))

(defn context [name]
  (-> name
      util/loader
      merge-context-utils))

(defn blank? [text]
  ;; (println "blank? received" text (or (nil? text)(= "" text)))
  (or (nil? text)
      (= "" text)))

(defn simple-list [label modifier]
  (fn [map]
    (format "%s %s"
            (str/capitalize (label map))
            (format-mod (modifier map)))))

(defn complex-list [resistance]
  (cond
   (string? resistance) resistance
   (map? resistance) (format "%s %s"
                             (util/join-sentence (:types resistance))
                             (:details resistance))))

(defn damage-resistances-list [resistances]
  (fn [text]
    (str/join "; " (map complex-list resistances))))

(defn skills-list [skills]
  (fn [text]
    (str/join ", " (map (simple-list :name :modifier) skills))))

(defn saves-list [saves]
  (fn [text]
    (str/join ", " (map (simple-list :ability :modifier) saves))))

(defn list-or [list default]
  (if (empty? list)
    default
    (str/join ", " list)))

(defn alt-speed [movement]
  (let [details (if (:details movement) (str " " (:details movement)) "")]
    (format "%s %d ft.%s" (:name movement) (:value movement) details)))

(defn speed [movements]
  (fn [text]
    (let [[bases alts] (partition-by #(= (:name %) "base") movements)
          base (first bases)
          base-str (str (:value base) " ft.")]
      (str/join ", " (into [base-str] (map #(alt-speed %) alts))))))

(defn reach-and-range [text]
  "?? ft.")

(defn add-filters [ctx]
  (sf/add-filter! :blank? blank?)
  (sf/add-filter! :present? #(not (blank? %)))
  (sf/add-filter! :markdown str)
  (sf/add-filter! :format str)
  (sf/add-filter! :list-or list-or)
  (sf/add-filter! :mean-roll util/mean-roll)
  (sf/add-filter! :ability-mod calc-ability-mod)
  (sf/add-filter! :mod format-mod)
  (sf/add-filter! :speed (speed (:movements ctx)))
  (sf/add-filter! :saves-list (saves-list (:saving-throws ctx)))
  (sf/add-filter! :skills-list (skills-list (:skills ctx)))
  (sf/add-filter! :damage-resistances-list (damage-resistances-list (:damage-resistances ctx))))


(defn render [& names]
  (doseq [name names]
    (let [ctx (context name)]
      (add-filters ctx)
      (spit (str name ".tex")
            (print-str (sp/render (load-template) ctx options))))))
