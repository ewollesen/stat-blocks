(defproject stat-blocks "0.1.0-SNAPSHOT"
  :description "Role-playing game statistics block generator"
  :url "https://github.com/ewollesen/stat-blocks"
  :license {:name "GNU General Public License Version 3"
            :url "https://www.gnu.org/licenses/gpl.txt"}
  :dependencies [[org.clojure/clojure "1.6.0"]
                 [de.ubercode.clostache/clostache "1.4.0"]
                 [selmer "0.7.2"]
                 [markdown-clj "0.9.55"]
                 [org.clojure/tools.cli "0.3.1"]
                 [org.clojure/data.json "0.2.5"]
                 [com.taoensso/timbre "3.3.1"]
                 [me.raynes/fs "1.4.4"]
                 [ring/ring-core "1.3.1"]
                 [ring/ring-jetty-adapter "1.3.1"]
                 [ring/ring-json "0.3.1"]
                 [org.clojure/math.numeric-tower "0.0.4"]]
  :main ^:skip-aot stat-blocks.core
  :plugins [[lein-ring "0.8.11"]]
  :bin {:name "stat-blocks"}
  :target-path "target/%s"
  :ring {:handler stat-blocks.http/app
         :port 3333}
  :profiles {:uberjar {:aot :all}
             :dev {:dependencies [[ring/ring-devel "1.3.1"]]}})
