(ns bedtime.core
  (:require [seesaw.core :as s]
            [seesaw.mig :as sm]
            [seesaw.behave :refer [when-focused-select-all]]
            [clj-time.core :as t] 
            [me.raynes.conch :refer [programs]]))

(s/native!)

(programs osascript)

(defn calculate-time [time]
  (let [[hours minute seconds] (map #(Long. %) (.split time ":"))
        now (t/now)]
    (t/in-msecs
     (t/interval now
                 (t/plus now
                         (t/hours hours)
                         (t/minutes minute)
                         (t/secs seconds))))))

(defn sleep-event [t]
  (fn [e]
    (let [time (s/text t)]
      (future
        (Thread/sleep (calculate-time time))
        (osascript "-e" "tell application \"System Events\" to sleep")))))

(defn time-field []
  (let [t (s/text :text "h:m:s"
                  :editable? true)]
    (doto t
      (when-focused-select-all))))

(defn layout []
  (let [t (time-field)]
    (sm/mig-panel
     :items [[(s/label "Time: ") "grow"]
             [t "wmin 100, wrap"]
             [(s/button :text "Night!"
                        :listen [:action (sleep-event t)])
              "grow, span"]])))

(defn frame []
  (doto (s/frame
         :title "Bedtime"
         :on-close :dispose
         :content (layout)
         :resizable? true)
    (s/pack!)
    (s/show!)))