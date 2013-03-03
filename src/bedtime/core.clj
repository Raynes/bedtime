(ns bedtime.core
  (:require [seesaw.core :as s]
            [seesaw.mig :as sm]
            [seesaw.behave :refer [when-focused-select-all]]
            [clj-time.core :as t]
            [clj-time.format :as f]
            [me.raynes.conch :refer [programs]]
            [overtone.at-at :refer [mk-pool after stop]]))

(s/native!)

(programs osascript)

(def pool (mk-pool))

(def timer (atom {:job nil}))

(defn validate-input [t]
  (try (f/parse (f/formatters :hour-minute-second) t)
       (catch IllegalArgumentException _
         (s/alert "Time must be of hour:minute:second format!"))))

(defn calculate-time [time]
  (when-let [parsed (validate-input time)]
    (let [now (t/now)
          [hour minute second] ((juxt t/hour t/minute t/sec) parsed)
          then (t/plus now (t/hours hour) (t/minutes minute) (t/secs second))
          interval (t/interval now then)]
      {:now now
       :then then
       :ms (t/in-msecs interval)
       :interval interval})))

(declare new-timer)

(defn countdown [frame timer-map]
  (let [{:keys [then interval]} timer-map
        panel (sm/mig-panel
               :items [[(s/label "foo") "align center, wrap"]
                       [(s/button :text "Stop"
                                  :listen [:action (fn [_]
                                                     (stop (:job @timer))
                                                     (doto frame
                                                       (.setContentPane (new-timer frame))
                                                       (s/pack!)))])]])]
    panel))

(defn sleep-event [frame t]
  (fn [e]
    (when-let [{:keys [ms] :as timer-map} (calculate-time (s/text t))]
      (swap! timer assoc
             :job (after ms
                         #(osascript "-e" "tell application \"System Events\" to sleep")
                         pool))
      (doto frame
        (.setContentPane (countdown frame timer-map))
        (s/pack!)))))

(frame)
(defn time-field []
  (let [t (s/text :text "1:0:0"
                  :editable? true)]
    (doto t
      (when-focused-select-all))))

(defn new-timer [frame]
  (let [t (time-field)]
    (sm/mig-panel
     :items [[(s/label "Time: ") "grow"]
             [t "wmin 100, wrap"]
             [(s/button :text "Night!"
                        :listen [:action (sleep-event frame t)])
              "grow, span"]])))

(defn frame []
  (let [f (s/frame
           :title "Bedtime"
           :on-close :dispose
           :resizable? true)]
    (doto f
      (.setContentPane (new-timer f))
      (s/pack!)
      (s/show!))))