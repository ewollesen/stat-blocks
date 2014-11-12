(ns stat-blocks.html
  (:use clostache.parser
        [markdown.core :only [md-to-html-string]])
  (:require [clojure.edn :as edn]
            [clojure.java.io :as io]
            [clojure.string :as str]

            [stat-blocks.util :as util]))



;; (defn load-partial [name]
;;   {(keyword name) (-> (str name ".html.mustache")
;;                       io/resource
;;                       io/file
;;                       slurp)})

;; (defn has-check [context]
;;   (fn [key] (not (empty? (key context)))))

;; (defn average-die-roll-core [text]
;;   (println "average-die-roll" text)
;;   (if (< 0 (count text))
;;     (do (println "go!")
;;         (let [[_ rolls sides sign mod] (re-find util/die-roll-re text)
;;               op (if (= "-" sign) - +)
;;               avg (+ (quot (Integer. (or sides 0)) 2) 1/2)
;;               _ (println "text" text "rolls" rolls "sides" sides)]
;;           (if sides
;;             (quot (op (* (Integer. (or rolls 1)) avg) (Integer. (or mod 0))) 1)
;;             "")))
;;     ""))

;; (defn average-die-roll [template]
;;   (fn [render-fn]
;;     (let [roll (render-fn template)]
;;       (average-die-roll roll))))

;; (defn format-reach-or-range [[key val]]
;;   (cond
;;    (= key :reach) (str "reach " val " ft.")
;;    (= key :range) (if-let [[_ short long] (re-find util/range-re (or val ""))]
;;                     (str "range " short "/" long " ft.")
;;                     nil)))

;; (defn markdown-with-p [template]
;;   (fn [render-fn]
;;     (let [text (render-fn template)]
;;       (if (< 0 (count text))
;;         (md-to-html-string text :paragraph? false :last-line-emtpy? false)
;;         ""))))

;; (defn markdown [template]
;;   (fn [render-fn]
;;     (let [text (render-fn template)]
;;       (if (< 0 (count text))
;;         (md-to-html-string text :paragraph? true :last-line-emtpy? false)
;;         ""))))

;; (defn damage-details [template]
;;   (fn [render-fn]
;;     (let [details (render-fn template)]
;;       (str (str/join ", " (filter #(< 0 (count %)) ["damage" details])) "."))))

;; (defn reach-or-range-core [text]
;;   (let [[_ reach range] (re-find util/reach-or-range-re text)
;;         args {:reach reach :range range}]
;;     (str/join " or " (filter identity (map format-reach-or-range args)))))

;; (defn reach-or-range [template]
;;   (fn [render-fn]
;;     (let [text (render-fn template)]
;;       (reach-or-range-core text))))

;; (defn mod-format [template]
;;   (fn [render-fn]
;;     (-> template
;;         render-fn
;;         str/trim
;;         Integer.
;;         util/format-mod)))

;; (defn content-tag
;;   ([name value]
;;      (content-tag name value {}))
;;   ([name value options]
;;      (let [cls (if (:class options)
;;                  (str " class=\"" (:class options) "\"")
;;                  "")]
;;        (str "<" name cls ">" value "</" name ">"))))

;; (defn join-list [xs]
;;   (->> xs
;;        (map #(content-tag "li" %))
;;        (str/join "")))

;; (defn render-resistance [resistance]
;;   (cond (string? resistance) (str resistance)
;;         (map? resistance) (let [types (join-list (:types resistance))
;;                                 details (:details resistance)]
;;                             (content-tag "ul" (str types " " details) {:class "inline"}))))

;; (defn generate-damage-resistances-list [context]
;;   (fn []
;;     (str "<ul class=\"top-list inline\">"
;;          (str/join "" (map #(content-tag "li" (render-resistance %))
;;                            (:damage-resistances context)))
;;          "</ul>")))

;; (defn generate-speed [speed]
;;   (let [details (when (< 0 (count (:details speed)))
;;                   (str " " (:details speed)))]
;;     (if (= (:name speed) "base")
;;       (str "<li>" (:value speed) " ft." details "</li>")
;;       (str "<li>" (:name speed) " " (:value speed) " ft." details "</li>"))))

;; (defn generate-speeds [context]
;;   (fn []
;;     (let [base (filter #(= "base" (:name %)) (:movements context))
;;           alts (filter #(not= "base" (:name %)) (:movements context))]
;;       (str/join ", " (map generate-speed (flatten (conj (conj [] base) alts)))))))

;; (defn merge-utils [context]
;;   (merge context {:format mod-format
;;                   :reach-or-range reach-or-range
;;                   :speeds (generate-speeds context)
;;                   :damage-resistances-list (generate-damage-resistances-list context)
;;                   :markdown markdown
;;                   :markdown-with-p markdown-with-p
;;                   :damage-details damage-details
;;                   :average-die-roll average-die-roll}))

;; (defn calc-ability-modifier [score]
;;   (util/format-mod (quot (- score 10) 2)))

;; (defn mod [template]
;;   (fn [render-fn]
;;     (-> template
;;         render-fn
;;         str/trim
;;         Integer.
;;         calc-ability-modifier)))

;; (defn merge-ability-modifiers [context]
;;   (merge context {:mod mod}))

;; (defn merge-existence-checks [context]
;;   (let [has? (has-check context)]
;;     (merge context {
;;                     :has-ac-details? (has? :ac-details)
;;                     :has-actions? (has? :actions)
;;                     :has-condition-immunities? (has? :condition-immunities)
;;                     :has-damage-immunities? (has? :damage-immunities)
;;                     :has-damage-resistances? (has? :damage-resistances)
;;                     :has-damage-vulnerabilities? (has? :damage-vulnerabilities)
;;                     :has-legendary-actions? (has? :legendary-actions)
;;                     :has-reactions? (has? :reactions)
;;                     :has-saving-throws? (has? :saving-throws)
;;                     :has-senses? (has? :senses)
;;                     :has-skills? (has? :skills)
;;                     :has-speed-details? (has? :speed-details)
;;                     })))

;; (defn process-reach-or-range [action]
;;   (str/join "/") (into [(:reach action)]
;;                        (map #(str/join "/" (str %)) (or (:range action) []))))

;; (defn merge-reach-or-range [context]
;;   (merge context {:actions (map process-reach-or-range (:actions context))}))

;; (def partials (merge {}
;;                      (load-partial "stat-block")))
