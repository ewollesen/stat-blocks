(ns stat-blocks.core
  (:require [clojure.string :as str]
            [clojure.tools.cli :refer [parse-opts]]

            [stat-blocks.renderer :refer [render render-opts]]
            [stat-blocks.loader :as loader])
  (:gen-class))


(def cli-options
  [["-o" "--output FILENAME" "Output filename (w/o extension)"
    :default "monsters"
    :parse-fn str]
   ["-c" "--color" "Generates full color output"]
   ;; ["-f" "--format" "Select the output format, pdf or png."
   ;;  :default "pdf"]
   [nil "--png" "Generates PNG output, one file per stat-block"
    :default false]
   [nil "--pdf" "Generates PDF output, one file for all stat-blocks"
    :default true]
   ["-h" "--help"]])
(def error-no-files "You must provide at least one monster datafile.")


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

(defn try-render [options filenames]
  (try
    (render options (loader/load filenames))
    0
    (catch Exception e 1)))

(defn -main [& args]
  (let [{:keys [options arguments errors summary]} (parse-opts args cli-options)]
    (cond
     (:help options) (exit 0 (usage summary))
     (empty? arguments) (exit 1 (error-msg [error-no-files]))
     errors (exit 1 (error-msg errors)))

    (System/exit (try-render options arguments))))
