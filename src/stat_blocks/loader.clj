(ns stat-blocks.loader
  (:require [clojure.java.io :as io]
            [clojure.edn :as edn]))


(defn load-edn [filename]
  (try
    (with-open [r (-> filename
                      io/reader
                      java.io.PushbackReader.)]
      (edn/read r))
    (catch Exception e (println "Error loading edn file:" filename))))

(defn load [filenames]
  (map load-edn filenames))
