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
                 [me.raynes/fs "1.4.4"]]
  :main ^:skip-aot stat-blocks.core
  :target-path "target/%s"
  :profiles {:uberjar {:aot :all}})
