(ns stat-blocks.renderer
  (:use [clojure.java.shell :only [sh]])
  (:require [clojure.java.io :as io]
            [clojure.string :as str]
            [me.raynes.fs :as fs]
            [stat-blocks.selmer :as selmer]
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

(defn move-output [from to]
  (let [files (fs/glob from "*.{png,pdf}")]
    (mapv #(fs/copy % (io/file fs/*cwd* (fs/base-name %))) files)
    (fs/delete-dir from)))

(defn render [raw-options monsters]
  (let [work-dir (fs/temp-dir "stat-blocks-")
        tex-file (io/file work-dir (str (:output raw-options) ".tex"))
        opts (render-opts raw-options)]
    (spit tex-file
          (selmer/render latex-template
                         (assoc opts :monsters monsters)))
    (my-sh "pdflatex"
           "-interaction=nonstopmode"
           "-shell-escape"
           (.getAbsolutePath tex-file)
           :dir (.getAbsolutePath work-dir))
    (move-output work-dir fs/*cwd*)))