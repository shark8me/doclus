(defproject doclus "0.1.0-SNAPSHOT"

  :description "FIXME: write description"
  :url "http://example.com/FIXME"

  :dependencies [[clj-time "0.14.0"]
                 [compojure "1.6.0"]
                 [cprop "0.1.10"]
                 [funcool/struct "1.0.0"]
                 [luminus-immutant "0.2.3"]
                 [luminus-nrepl "0.1.4"]
                 [luminus/ring-ttl-session "0.3.2"]
                 [markdown-clj "0.9.99"]
                 [metosin/muuntaja "0.3.2"]
                 [metosin/ring-http-response "0.9.0"]
                 [mount "0.1.11"]
                 [org.clojure/clojure "1.8.0"]
                 [org.clojure/tools.cli "0.3.5"]
                 [org.clojure/tools.logging "0.4.0"]
                 [org.webjars.bower/tether "1.4.0"]
                 [org.clojure/clojurescript "1.9.473"]
                 [org.webjars/bootstrap "4.0.0-alpha.5"]
                 [org.webjars/font-awesome "4.7.0"]
                 [org.webjars/jquery "3.2.1"]
                 [ring-webjars "0.2.0"]
                 [ring/ring-core "1.6.2"]
                 [quil "2.6.0"]
                 [ring/ring-defaults "0.3.1"]
                 [selmer "1.11.0"]

                 ;;word2vec
                 [org.bridgei2i/word2vec "0.2.1"]
                 [dl4clj "0.0.2"]
                 [net.mikera/core.matrix "0.60.3"]

                 ;;sql
                 [postgresql "9.3-1102.jdbc41"]
                 [org.clojure/java.jdbc "0.7.0"]
                 ]

  :min-lein-version "2.0.0"

  :jvm-opts ["-server" "-Dconf=.lein-env" "-Xmx12g"]
  :source-paths ["src/clj"]
  :test-paths ["test/clj"]
  :resource-paths ["resources" "target/cljsbuild"]
  :target-path "target/%s/"
  :main ^:skip-aot doclus.core

  :plugins [[lein-cprop "1.0.3"]
            [lein-immutant "2.1.0"]
            [lein-cljsbuild "1.1.3"]]

  :cljsbuild
  {:builds 
   {:app {:source-paths  ["src/cljs"] 
          :compiler {;:main          (str project-ns ".app") 
                     :asset-path    "/js/out" 
                     :output-to     "target/cljsbuild/public/js/app.js" 
                     :output-dir    "target/cljsbuild/public/js/out" 
                     :optimizations :none 
                     :main "doclus.core"
                     :source-map    true 
                     :pretty-print  true}} 
    :min {:source-paths  ["src/cljs"] 
          :compiler {:output-to     "target/cljsbuild/public/js/app.js" 
                     :output-dir    "target/uberjar" 
                     :externs       ["react/externs/react.js"] 
                     :optimizations :advanced 
                     :pretty-print  false}}}}

  :profiles
  {:uberjar {:omit-source true
             :aot :all
             :uberjar-name "doclus.jar"
             :source-paths ["env/prod/clj"]
             :resource-paths ["env/prod/resources"]}

   :dev           [:project/dev :profiles/dev]
   :test          [:project/dev :project/test :profiles/test]

   :project/dev  {:dependencies [[prone "1.1.4"]
                                 [ring/ring-mock "0.3.1"]
                                 [ring/ring-devel "1.6.2"]
                                 [pjstadig/humane-test-output "0.8.2"]]
                  :plugins      [[com.jakemccrary/lein-test-refresh "0.19.0"]]
                  
                  :source-paths ["env/dev/clj"]
                  :resource-paths ["env/dev/resources"]
                  :repl-options {:init-ns user}
                  :injections [(require 'pjstadig.humane-test-output)
                               (pjstadig.humane-test-output/activate!)]}
   :project/test {:resource-paths ["env/test/resources"]}
   :profiles/dev {}
   :profiles/test {}})
