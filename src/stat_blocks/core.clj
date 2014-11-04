(ns stat-blocks.core
  (:use clostache.parser
        [markdown.core :only [md-to-html-string]])
  (:require [clojure.edn :as edn]
            [clojure.java.io :as io]
            [clojure.string :as str])
  (:gen-class))


(def die-roll-re #"(\d+)?d(\d+)(?:\s*(\+|-)\s*(\d+))?")
(def num-format (partial format "%+d"))
(def range-re #"([0-9]+)/([0-9]+)")
(def reach-or-range-re #"([0-9]+)/([0-9]+/[0-9]+/)?")


(defn loader [name]
  (with-open [r (-> (str name ".edn")
                    io/resource
                    io/reader
                    java.io.PushbackReader.)]
    (edn/read r)))

(defn load-partial [name]
  {(keyword name) (-> (str name ".html.mustache")
                      io/resource
                      io/file
                      slurp)})

(defn has-check [context]
  (fn [key] (not (empty? (key context)))))

(defn average-die-roll [template]
  (fn [render-fn]
    (let [roll (render-fn template)
          [_ rolls sides sign mod] (re-find die-roll-re roll)
          op (if (= "-" sign) - +)
          avg (+ (quot (Integer. (or sides 0)) 2) 1/2)]
      (if sides
        (quot (op (* (Integer. (or rolls 1)) avg) (Integer. (or mod 0))) 1)
        ""))))

(defn format-reach-or-range [[key val]]
  (cond
   (= key :reach) (str "reach " val " ft.")
   (= key :range) (if-let [[_ short long] (re-find range-re (or val ""))]
                    (str "range " short "/" long " ft.")
                    nil)))

(defn markdown-with-p [template]
  (fn [render-fn]
    (let [text (render-fn template)]
      (if (< 0 (count text))
        (md-to-html-string text :paragraph? false :last-line-emtpy? false)
        ""))))

(defn markdown [template]
  (fn [render-fn]
    (let [text (render-fn template)]
      (if (< 0 (count text))
        (md-to-html-string text :paragraph? true :last-line-emtpy? false)
        ""))))

(defn damage-details [template]
  (fn [render-fn]
    (let [details (render-fn template)]
      (str (str/join ", " (filter #(< 0 (count %)) ["damage" details])) "."))))

(defn reach-or-range [template]
  (fn [render-fn]
    (let [text (render-fn template)
          [_ reach range] (re-find reach-or-range-re text)
          args {:reach reach :range range}]
      (str/join " or " (filter identity (map format-reach-or-range args))))))

(defn mod-format [template]
  (fn [render-fn]
    (-> template
        render-fn
        str/trim
        Integer.
        num-format)))

(defn content-tag
  ([name value]
     (content-tag name value {}))
  ([name value options]
     (let [cls (if (:class options)
                 (str " class=\"" (:class options) "\"")
                 "")]
       (str "<" name cls ">" value "</" name ">"))))

(defn join-list [xs]
  (->> xs
       (map #(content-tag "li" %))
       (str/join "")))

(defn render-resistance [resistance]
  (cond (string? resistance) (str resistance)
        (map? resistance) (let [types (join-list (:types resistance))
                                details (:details resistance)]
                            (content-tag "ul" (str types " " details) {:class "inline"}))))

(defn generate-damage-resistances-list [context]
  (fn []
    (str "<ul class=\"top-list inline\">"
         (str/join "" (map #(content-tag "li" (render-resistance %))
                           (:damage-resistances context)))
         "</ul>")))

(defn generate-speed [speed]
  (let [details (when (< 0 (count (:details speed)))
                  (str " " (:details speed)))]
    (if (= (:name speed) "base")
      (str "<li>" (:value speed) " ft." details "</li>")
      (str "<li>" (:name speed) " " (:value speed) " ft." details "</li>"))))

(defn generate-speeds [context]
  (fn []
    (let [base (filter #(= "base" (:name %)) (:movements context))
          alts (filter #(not= "base" (:name %)) (:movements context))]
      (str/join ", " (map generate-speed (flatten (conj (conj [] base) alts)))))))

(defn merge-utils [context]
  (merge context {:format mod-format
                  :reach-or-range reach-or-range
                  :speeds (generate-speeds context)
                  :damage-resistances-list (generate-damage-resistances-list context)
                  :markdown markdown
                  :markdown-with-p markdown-with-p
                  :damage-details damage-details
                  :average-die-roll average-die-roll}))

(defn calc-ability-modifier [score]
  (num-format (quot (- score 10) 2)))

(defn mod [template]
  (fn [render-fn]
    (-> template
        render-fn
        str/trim
        Integer.
        calc-ability-modifier)))

(defn merge-ability-modifiers [context]
  (merge context {:mod mod}))

(defn merge-existence-checks [context]
  (let [has? (has-check context)]
    (merge context {
                    :has-ac-details? (has? :ac-details)
                    :has-actions? (has? :actions)
                    :has-condition-immunities? (has? :condition-immunities)
                    :has-damage-immunities? (has? :damage-immunities)
                    :has-damage-resistances? (has? :damage-resistances)
                    :has-damage-vulnerabilities? (has? :damage-vulnerabilities)
                    :has-legendary-actions? (has? :legendary-actions)
                    :has-reactions? (has? :reactions)
                    :has-saving-throws? (has? :saving-throws)
                    :has-senses? (has? :senses)
                    :has-skills? (has? :skills)
                    :has-speed-details? (has? :speed-details)
                    })))

(def partials (merge {}
                     (load-partial "stat-block")))
(defn -main
  "I don't do a whole lot ... yet."
  [& args]
  (doseq [name args]
    (spit (str name ".html")
          (print-str (render-resource "page.html.mustache"
                                      (-> name
                                          loader
                                          merge-existence-checks
                                          merge-ability-modifiers
                                          merge-utils)
                                      partials)))))
