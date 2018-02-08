(ns spamalot.core
  "Contains the core functions for namespace `spamalot.core`."
  (:use tupelo.core)
  (:require
    [clojure.string :as str]
    [schema.core :as s]
    [tupelo.schema :as tsk]
    ))

;-----------------------------------------------------------------------------
(def emails-seen (atom nil))

(defn email-seen-reset! []
  (reset! emails-seen #{}))
(email-seen-reset!)

(s/defn record-email
  [email :- s/Str]
  (swap! emails-seen conj email))

(s/defn seen-email? :- s/Bool
  [email :- s/Str]
  (contains? @emails-seen email))

;-----------------------------------------------------------------------------
(def window-size 4)
(def window (atom nil))

(defn window-reset! []
  (reset! window clojure.lang.PersistentQueue/EMPTY))
(window-reset!)

(defn add-to-window
  [arg]
  (swap! window conj arg)
  (while (< window-size (count @window))
    (swap! window pop)))

;-----------------------------------------------------------------------------
(def max-spam-score 0.3)
(s/defn non-spammy-email :- s/Bool
  [email-rec :- tsk/Map ]
  (<= (grab :spam-score email-rec) max-spam-score))

;-----------------------------------------------------------------------------
(def cum-spam-score-max 0.05)
(def cum-stats (atom nil))

(def CumStats {:cum-spam-score s/Num
               :cum-num-emails s/Int})

(s/defn cum-stats-reset! :- CumStats
  []
  (reset! cum-stats {:cum-spam-score 0.0
                     :cum-num-emails 0}))
(cum-stats-reset!)

(s/defn calc-cum-stats
  [cum-stats-curr :- CumStats
   email-rec :- tsk/Map]
  (it-> cum-stats-curr
    (update it :cum-spam-score + (grab :spam-score email-rec))
    (update it :cum-num-emails inc)))

(s/defn new-email-ok-cum-stats? :- s/Bool
  [email-rec :- tsk/Map]
  (let [cum-stats-new (calc-cum-stats @cum-stats email-rec)
        cum-spam-score (/ (grab :cum-spam-score cum-stats-new)
                          (grab :cum-num-emails cum-stats-new)) ]
    (<= cum-spam-score cum-spam-score-max)))

(s/defn accumulate-email-stats
  [email-rec :- tsk/Map]
  (swap! cum-stats #(calc-cum-stats % email-rec)))