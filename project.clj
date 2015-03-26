(defproject redis-test "0.1.0-SNAPSHOT"
  :description "FIXME: write description"
  :url "http://example.com/FIXME"
  :license {:name "Eclipse Public License"
            :url "http://www.eclipse.org/legal/epl-v10.html"}
  :dependencies [[org.clojure/clojure "1.6.0"]
                 [redis-async "0.1.0-SNAPSHOT"]
                 [com.taoensso/carmine "2.9.0"]
                 [criterium "0.4.3"]]
  :jvm-opts ^:replace [])
