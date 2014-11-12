(ns stat-blocks.http
  (use [ring.util.response :only [header response file-response]]
       [ring.middleware.json :only [wrap-json-params]])
  (require [clojure.string :as str]
           [ring.middleware.keyword-params :refer [wrap-keyword-params]]
           [me.raynes.fs :as fs]

           [stat-blocks.renderer :refer [render]]
           [stat-blocks.loader :refer [load-filenames]]))


(defn wrap-disposition [name response]
  (header response "Content-Disposition" (format "inline; filename=%s" name)))

(defn handler [{params :params :as request}]
  (try
    (let [monsters (if (empty? params)
                    (load-filenames ["resources/sample.edn"])
                    [params])
          name (str/replace (:name (first monsters)) #"[\W\.]+" "-")
          http-opts {:png true :output name}
          output (render http-opts monsters)
          filename (first (filter #(.endsWith % "-0.png") output))]
      (->> output
           (filter #(.endsWith % "-0.png"))
           first
           file-response
           (wrap-disposition (str name ".png"))))
    (catch Exception e (response (str "Failed!" e)))))

(def app
  (-> handler
      wrap-keyword-params
      wrap-json-params))
