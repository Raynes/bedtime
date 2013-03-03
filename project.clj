(defproject bedtime "0.1.0"
  :description "A sleep timer for your computer."
  :url "https://github.com/Raynes/bedtime"
  :license {:name "Eclipse Public License"
            :url "http://www.eclipse.org/legal/epl-v10.html"}
  :dependencies [[org.clojure/clojure "1.5.0"]
                 [seesaw "1.4.3"]
                 [com.miglayout/miglayout "3.7.4"]
                 [clj-time "0.4.4"]
                 [me.raynes/conch "0.5.1"]
                 [overtone/at-at "1.1.1"]]
  :main bedtime.main/-main)
