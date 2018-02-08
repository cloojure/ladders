(ns spamalot.core
  "Contains the core functions for namespace `spamalot.core`."
  (:use tupelo.core)
  (:require
    [clojure.string :as str]
    [schema.core :as s]
    [tupelo.schema :as tsk]
    ))

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

(def max-spam-score 0.3)
(s/defn non-spammy-email :- s/Bool
  [email-rec :- tsk/Map ]
  (<= (grab :spam-score email-rec) max-spam-score))