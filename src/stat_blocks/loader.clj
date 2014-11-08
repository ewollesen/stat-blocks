(ns stat-blocks.loader
  (:require [clojure.java.io :as io]
            [clojure.edn :as edn]
            [clojure.data.json :as json]))

(defn load-filename [loader filename]
  (try
    (with-open [r (-> filename
                      io/reader
                      java.io.PushbackReader.)]
      (loader r))
    (catch Exception e (println "Error loading file:" filename))))

(defn load-json [filename]
  (load-filename (fn [x] (json/read x :key-fn keyword)) filename))

(defn load-edn [filename]
  (load-filename edn/read filename))

(defn load [filenames]
  (map load-json filenames))
