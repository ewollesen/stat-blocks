(ns stat-blocks.http
  (use [ring.util.response :only [header response file-response]]
       [ring.middleware.json :only [wrap-json-params]]
       )
  (require [clojure.string :as str]
           [clojure.java.io :as io]
           [ring.util.request :refer [content-type urlencoded-form?]]
           [ring.middleware.keyword-params :refer [wrap-keyword-params]]
           [ring.middleware.stacktrace :refer [wrap-stacktrace]]
           [ring.middleware.params :refer [wrap-params]]
           [ring.middleware.nested-params :refer [wrap-nested-params]]
           [me.raynes.fs :as fs]

           [stat-blocks.renderer :refer [render]]
           [stat-blocks.loader :refer [load-filenames]]))



(defn print-n-return [object]
  (clojure.pprint/pprint object)
  object)

(defn wrap-disposition [name response]
  (header response "Content-Disposition" (format "inline; filename=%s" name)))

(defn display-png [params]
  (try
    (let [monsters (if (empty? params)
                     (load-filenames ["resources/sample.edn"])
                     [params])
          name (str/replace (:name (first monsters)) #"[\W\.]+" "")
          http-opts {:png true :output name}
          output (render http-opts monsters)
          filename (first (filter #(.endsWith % "-0.png") output))]
      (->> output
           (filter #(.endsWith % "-0.png"))
           first
           file-response
           (wrap-disposition (str name ".png"))))))

(defn display-form []
  (response (slurp (io/resource "input-form.html"))))

(defn empty-vec? [vec]
  (every? empty? vec))

(defn my-empty? [obj]
  (cond
   (string? obj) (empty? obj)
   (vector? obj) (empty-vec? obj)
   :else true))

(defn empty-map? [map]
  (every? my-empty? (vals map)))

(defn stripper [old]
  (filterv #(not (empty-map? %)) old))

(defn strip-empty-maps [params & ks]
  (update-in params ks stripper))

(defn strip-empty-vecs [params & ks]
  (let [f (fn [old]
            (clojure.pprint/pprint old)
            (print-n-return (vec (remove empty? old))))]
    (update-in params ks f)))

(defn strip-fields [params]
  (-> params
      (strip-empty-maps :saving-throws)
      (strip-empty-maps :skills)
      (strip-empty-maps :traits)
      (strip-empty-vecs :actions 0 :range)
      (strip-empty-maps :actions)
      (strip-empty-maps :reactions)
      (strip-empty-maps :legendary-actions :actions)))

(defn splitter [old]
  (filterv #(not (empty? %)) (str/split (str old) #"\s*(,|and|or|;)\s*")))

(defn split-field [params & ks]
  (update-in params ks splitter))

(defn split-fields [params]
  (-> params
      (split-field :damage-vulnerabilities)
      (split-field :damage-resistances)
      (split-field :damage-immunities)
      (split-field :condition-immunities)
      (split-field :senses)
      (split-field :languages)))

(defn vectifier [old]
  (if (every? #(re-matches #"^[0-9]+$" (name %)) (keys old))
    (vec (vals old))
    old))

(defn intkeys->vec [params & ks]
  (update-in params ks vectifier))

(defn construct-vecs [params]
  (-> params
      (intkeys->vec :movements)
      (intkeys->vec :saving-throws)
      (intkeys->vec :skills)
      (intkeys->vec :traits)
      (intkeys->vec :actions)
      (intkeys->vec :reactions)
      (intkeys->vec :legendary-actions :actions)))

(defn process-form-params [params]
  (clojure.pprint/pprint params)
  (print-n-return (-> params
                      :monster
                      construct-vecs
                      split-fields
                      strip-fields)))

(defn json? [request]
  (= "application/json" (content-type request)))

(defn handler [{params :params :as request}]
  (if (= (:request-method request) :get)
    (display-form)
    (display-png (if (json? request)
                   params
                   (process-form-params params)))))


(def app
  (-> handler
      wrap-stacktrace
      wrap-keyword-params
      wrap-nested-params
      wrap-params
      wrap-json-params))
