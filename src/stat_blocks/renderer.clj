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
      (println (:out result))
      (println (:err result))
      ;; TODO don't exit, raise
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
    (my-sh "xelatex"
           "-interaction=nonstopmode"
           (fs/base-name tex-file)
           :dir work-dir)
    (when (:png? opts)
      (my-sh "convert"
             "-quality" "90"
             "-density" "300x300"
             (with-ext "pdf" (fs/base-name tex-file))
             (str (:output raw-options) "-%d.png")
             :dir work-dir))
    (map #(.getAbsolutePath %) (fs/glob work-dir "*.{png,pdf}"))))
