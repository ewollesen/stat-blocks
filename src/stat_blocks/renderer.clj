(ns stat-blocks.renderer
  (:require [stat-blocks.selmer :as latex]))

(defn render [options monsters]
  (latex/render options monsters))
