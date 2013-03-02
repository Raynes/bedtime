(ns bedtime.core
  (:require [seesaw.core :as s]
            [seesaw.mig :as sm]
            [seesaw.behave :refer [when-focused-select-all]]
            [clj-time.core :as t]
            [clj-time.format :as f]
            [me.raynes.conch :refer [programs]]
            [overtone.at-at :refer [mk-pool after]]))

(s/native!)

(programs osascript)

(def pool (mk-pool))

(def timer (atom {:job nil}))

(defn validate-input [t]
  (try (f/parse (f/formatters :hour-minute-second) t)
       (catch IllegalArgumentException _
         (s/alert "Time must be of hour:minute:second format!"))))

(defn calculate-time [time]
  (when-let [[hour minute second] ((juxt t/hour t/minute t/secs)
                                   (validate-input time))]
    (let [now (t/now)]
      (t/in-msecs
       (t/interval now
                   (t/plus now
                           (t/hours hour)
                           (t/minutes minute)
                           (t/secs second)))))))

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