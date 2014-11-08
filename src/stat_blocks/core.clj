(ns stat-blocks.core
  (:use clostache.parser
        [markdown.core :only [md-to-html-string]])
  (:require [clojure.edn :as edn]
            [clojure.java.io :as io]
            [clojure.string :as str]

            [clojure.tools.cli :refer [parse-opts]]

            [stat-blocks.selmer :as latex]
            [stat-blocks.util :as util])
  (:gen-class))


(def cli-options
  [["-o" "--output FILENAME" "Output filename (w/o extension)"
    :default "monsters"
    :parse-fn str]
   ["-c" "--color" "Generates full color output"]
   ;; [nil "--png" "Generates PNG output, one file per stat-block"]
   ;; [nil "--pdf" "Generates PDF output, one file for all stat-blocks" :default true]
   ["-h" "--help"]])


(defn exit [value msg]
  (if (= 0 value)
    (println msg)
    (binding [*out* *err*]
      (println msg)))
  (System/exit value))

(defn usage [options-summary]
  (->> ["Generate monster statistics blocks."
        ""
        "Usage: stat-block [options] <filename...>"
        ""
        "Options:"
        options-summary]
       (str/join \newline)))

(defn error-msg [errors]
  (str "The following errors occurred while parsing your command:\n\n"
       (str/join \newline errors)))

(defn try-render [options arguments]
  (try
    (latex/render options arguments)
    0
    (catch Exception e 1)))

(def error-no-files "You must provide at least one monster datafile.")

(defn -main [& args]
  (let [{:keys [options arguments errors summary]} (parse-opts args cli-options)]
    (cond
     (:help options) (exit 0 (usage summary))
     (empty? arguments) (exit 1 (error-msg [error-no-files]))
     errors (exit 1 (error-msg errors)))

    (System/exit (try-render options arguments))))
