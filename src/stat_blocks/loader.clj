(ns stat-blocks.loader
  (:require [clojure.java.io :as io]
            [clojure.edn :as edn]
            [clojure.string :as str]
            [clojure.data.json :as json]))


(def ext-re #"\.([^\.]+)$")


(defn extname [filename]
  (last (re-find ext-re filename)))

(defn try-load [loader filename]
  (try
    (with-open [r (-> filename
                      io/reader
                      java.io.PushbackReader.)]
      (loader r))
    (catch Exception e (println "Error loading file:" filename))))

(defn load-json [filename]
  (try-load (fn [x] (json/read x :key-fn keyword)) filename))

(defn load-edn [filename]
  (try-load edn/read filename))

(defn load-filename [filename]
  (let [ext (str/lower-case (extname filename))]
    (cond (= ext "json") (load-json filename)
          (= ext "edn") (load-edn filename)
          :default (do (println "Don't know how to load:" filename)
                       nil))))

(defn load [filenames]
  (reduce (fn [m i] (if-let [data (load-filename i)]
                      (conj m data)
                      m))
          []
          filenames))
