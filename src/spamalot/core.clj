(ns spamalot.core
  "Contains the core functions for namespace `spamalot.core`."
  (:use tupelo.core)
  (:require
    [clojure.string :as str]
    [schema.core :as s]
    [tupelo.schema :as tsk]
  ))

;-----------------------------------------------------------------------------
(def Email {:email-address s/Str
            :spam-score    s/Num})

(def emails-seen (atom nil)) ; #todo how to tell Schema type = (atom {s/Str tsk/Map} )  ???

(defn email-seen-reset! []
  (reset! emails-seen {}))
(email-seen-reset!)

(s/defn record-email
  [email :- Email]
  (swap! emails-seen assoc (grab :email-address email) email))

(s/defn seen-email? :- s/Bool
  [email :- Email]
  (contains-key? @emails-seen (grab :email-address email)))

;-----------------------------------------------------------------------------
(def max-spam-score 0.3)
(s/defn non-spammy-email :- s/Bool
  [email :- Email ]
  (<= (grab :spam-score email) max-spam-score))

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
  (let [cum-stats-new  (calc-cum-stats @cum-stats email-rec)
        cum-spam-score (/ (grab :cum-spam-score cum-stats-new)
                         (grab :cum-num-emails cum-stats-new))]
    (<= cum-spam-score cum-spam-score-max)))

(s/defn accumulate-email-stats
  [email-rec :- tsk/Map]
  (swap! cum-stats calc-cum-stats email-rec))