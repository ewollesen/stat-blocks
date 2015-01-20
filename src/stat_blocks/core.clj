(ns stat-blocks.core
  (:require [clojure.string :as str]
            [clojure.tools.cli :refer [parse-opts]]
            [clojure.java.io :as io]

            [me.raynes.fs :as fs]

            [stat-blocks.http :as http]
            [stat-blocks.renderer :refer [render]]
            [stat-blocks.loader :as loader])
  (:gen-class))


(def cli-options
  [["-o" "--output FILENAME" "Output filename (w/o extension)"
    :default "monsters"
    :parse-fn str]
   ["-c" "--color" "Generates full color output"]
   [nil "--png" "Generates PNG output, one file per stat-block"
    :default false]
   [nil "--pdf" "Generates PDF output, one file for all stat-blocks"
    :default true]
   ["-s" "--serve" "Run Jetty server" :default false]
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

(defn move-to-cwd [src]
  (fs/copy src (io/file fs/*cwd* (fs/base-name src)))
  (fs/delete src))

(defn try-render [options filenames]
  (try
    (doseq [filename (render options (loader/load-filenames filenames))]
      (move-to-cwd filename))
    0
    (catch Exception e
      (println e)
      1)))

(defn serve-forever []
  )

(defn -main [& args]
  (let [{:keys [options arguments errors summary]} (parse-opts args cli-options)]
    (cond
     (:help options) (exit 0 (usage summary))
     (:serve options) (exit 0 (http/serve-forever))
     (empty? arguments) (exit 1 (error-msg [error-no-files]))
     errors (exit 1 (error-msg errors)))

    (System/exit (try-render options arguments))))
