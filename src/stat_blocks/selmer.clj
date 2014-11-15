(ns stat-blocks.selmer
  (:use [clojure.java.shell :only [sh]])
  (:require [clojure.edn :as edn]
            [clojure.java.io :as io]
            [clojure.string :as str]

            [selmer.parser :as sp]
            [selmer.filters :as sf]
            ;;[taoensso.timbre :as timbre]

            [stat-blocks.util :as util]))
;;(timbre/refer-timbre)


(def selmer-options {:tag-open \< :tag-close \>})
(def template "monster.tex.selmer")
;; (def template-grey "template-grey.tex.selmer")

(defn format-mod [text]
  (-> text
      util/str->int
      util/format-mod))

(defn calc-ability-mod [text]
  (-> text
      util/str->int
      util/calc-ability-mod
      util/format-mod))

(defn render-reach-and-range [action]
  (let [reach (when (contains? action :reach)
                (str "reach " (:reach action) " ft."))
        range (when (< 0 (count (get-in action [:range])))
                (str "range " (str/join "/" (:range action)) " ft."))]
    (str/join " or " (filter identity [reach range]))))

(defn merge-reach-and-range [monster]
  (let [f #(assoc % :reach-and-range (render-reach-and-range %))]
    (do
      (assoc monster :actions (mapv f (:actions monster))))))

(defn merge-monster-utils [monsters]
  (map (fn [monster] (-> monster
                         merge-reach-and-range))
       monsters))

(defn simple-list [label modifier]
  (fn [map]
    (format "%s %s"
            (str/capitalize (label map))
            (format-mod (modifier map)))))

(defn title [text]
  (str/replace text #"\b([a-z]+)" (comp str/capitalize last)))

(defn complex-list [resistance]
  (cond
   (string? resistance) resistance
   (map? resistance) (format "%s %s"
                             (util/join-sentence (:types resistance))
                             (:details resistance))))

(defn damage-resistances-list [resistances]
  (str/join "; " (map complex-list resistances)))

(defn skills-list [skills]
  (str/join ", " (map (simple-list :name :modifier) skills)))

(defn saves-list [saves]
  (str/join ", " (map (simple-list :ability :modifier) saves)))

(defn list-or [list default]
  (if (empty? list)
    default
    (str/join ", " list)))

(defn alt-speed [movement]
  (let [details (if (:details movement) (str " " (:details movement)) "")]
    (format "%s %d ft.%s" (:name movement) (:value movement) details)))

(defn speed [movements]
  (let [[bases alts] (partition-by #(= (:name %) "base") movements)
        base (first bases)
        base-str (str (:value base) " ft.")]
    (str/join ", " (into [base-str] (map #(alt-speed %) alts)))))

(defn fix-emph [text]
  (str/replace text
               #"\\emph\{([^}]*)\}"
               "{\\\\fontshape{it}\\\\selectfont $1}")
  ;; (str/replace (str/trim markdown) "\\emph{" "{\\em ")
  ;; (str/replace (str/trim markdown) "\\emph{" "\\textit{")
  ;; (str/replace markdown #"\\emph\{([^}]*)\}" "{\\\\em $1}")
  ;; (str/trim markdown)
  ;; text
  )

(defn kramdown [text]
  ;; kramdown renders *text* as \emph{text}, but in some places, this output
  ;; needs to get passed into a command, and they don't accept \emph{},
  ;; however, they *do* accept {\it text}, so swap them out.
  (let [markdown (:out (sh "kramdown" "-o" "latex" :in text))]
    (-> markdown
        str/trim
        fix-emph)))

(defn escape-latex-special-char [[all specific]]
  (cond
   (= specific "\\") "\\textbackslash{}"
   (= specific "^") "\\textasciicircum{}"
   (= specific "~") "\\textasciitilde{}"
   true (str "\\" specific)))

(defn sanitize-latex [text]
  (str/replace (str text) #"([\\\^\~{}\$\&\#_%])" escape-latex-special-char))

(defn latexify [target]
  (cond
   (string? target) (sanitize-latex target)
   (map? target) (into {} (for [[k v] target] [k (latexify v)]))
   (vector? target) (mapv latexify target)
   :else target))

(defn add-filters []
  (sf/add-filter! :ability-mod calc-ability-mod)
  (sf/add-filter! :blank? str/blank?)
  (sf/add-filter! :damage-resistances-list damage-resistances-list)
  (sf/add-filter! :format str)
  (sf/add-filter! :latex sanitize-latex)
  (sf/add-filter! :list-or list-or)
  (sf/add-filter! :markdown kramdown)
  (sf/add-filter! :mean-roll util/mean-roll)
  (sf/add-filter! :mod format-mod)
  (sf/add-filter! :present? #(not (empty? %)))
  (sf/add-filter! :saves-list saves-list)
  (sf/add-filter! :skills-list skills-list)
  (sf/add-filter! :speed speed)
  (sf/add-filter! :title title))

(defn render [template context]
  (sp/cache-off!)
  (add-filters)
  (let [monsters (merge-monster-utils (:monsters context))]
    (sp/render-file template (assoc context :monsters monsters) selmer-options)))
