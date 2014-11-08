(ns stat-blocks.loader
  (:require [clojure.java.io :as io]
            [clojure.edn :as edn]))


(defn load-edn [name]
  (try
    (with-open [r (-> (str name ".edn")
                     io/resource
                     io/reader
                     java.io.PushbackReader.)]
      (edn/read r))
    (catch Exception e (println "Error loading edn file:" (str name ".edn")))))

(defn load [names]
  (map load-edn names))
