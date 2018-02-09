(ns spamalot.window
  (:use tupelo.core)
  (:require
    [spamalot.core :refer [Email]]
    [clojure.string :as str]
    [schema.core :as s]
    [tupelo.schema :as tsk] ))

(def window-spam-score-max 0.1)
(def window-size-max 4)
(def window-state (ref nil))

(def empty-window clojure.lang.PersistentQueue/EMPTY)
(defn window-reset! [] (dosync (ref-set window-state empty-window)))
(window-reset!)

(defn ensure-window-size-max [window]
  (loop [window window]
    (if (<= (count window) window-size-max )
      window
      (recur (pop window)) )))

(defn add-email-to-window
  [window email]
  (ensure-window-size-max (conj window email)) )

(defn calc-window-spam-score
  [window]
  (let [cum-score (apply + (mapv #(grab :spam-score %) window))]
    (/ cum-score (count window))))

(defn spam-score-ok? [spam-score] (<= spam-score window-spam-score-max))

(s/defn new-email-ok-window? :- s/Bool
  [email :- Email]
  (let [window-new       (add-email-to-window @window-state email)
        window-score-new (calc-window-spam-score window-new)
        ok?              (spam-score-ok? window-score-new)]
    ok?))

(s/defn accumulate-window-emails [email :- Email]
  (dosync (alter window-state add-email-to-window email)))
