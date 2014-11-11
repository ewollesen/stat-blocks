(ns stat-blocks.renderer
  (:use [clojure.java.shell :only [sh]])
  (:require [stat-blocks.selmer :as selmer]
            [stat-blocks.util :refer [with-ext]]))


(def latex-template "block.tex.selmer")


(defn render-opts [options]
  (let [png? (:png options)
        pdf? (not (:png options))
        color? (:color options)]
    (-> options
        (assoc :pdf? pdf? :png? png? :color? color?)
        (dissoc :pdf :png :color))))

(defn my-sh [& args]
  (let [result (apply sh args)]
    (when (not= 0 (:exit result))
      (binding [*out* *err*] (println (:err result)))
      (System/exit (:exit result)))
    result))

(defn render [raw-options monsters]
  (let [filename (str (:output raw-options) ".tex")
        opts (render-opts raw-options)]
    (spit filename
          (selmer/render latex-template
                         (assoc opts :monsters monsters)))
    (my-sh "pdflatex" "-interaction=nonstopmode" "-shell-escape" filename)))
