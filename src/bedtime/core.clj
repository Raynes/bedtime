(ns bedtime.core
  (:require [seesaw.core :as s]
            [seesaw.mig :as sm]
            [seesaw.behave :refer [when-focused-select-all]]
            [clj-time.core :as t] 
            [me.raynes.conch :refer [programs]]
            [overtone.at-at :refer [mk-pool after]]))

(s/native!)

(programs osascript)

(def pool (mk-pool))

(def timer (atom {:job nil}))

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
      (swap! timer assoc
             :job (after (calculate-time time)
                         #(osascript "-e" "tell application \"System Events\" to sleep")
                         pool)))))

(defn time-field []
  (let [t (s/text :text "1:0:0"
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