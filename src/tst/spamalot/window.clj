(ns tst.spamalot.window
  (:use spamalot.window tupelo.core tupelo.test)
  (:require
    [clojure.spec.alpha :as sp]
    [clojure.spec.test.alpha :as stest]
    [clojure.spec.gen.alpha :as gen]))

; verify fifo window size when add new emails
(dotest
  (with-redefs [window-size-max 3]
    (nl) (window-reset!)
    (let [result (forv [ii (thru 7)]
                   (do
                     (dosync (alter window-state add-email-to-window {:email-address "x" :spam-score ii}))
                     [ii (mapv #(grab :spam-score %) @window-state) (calc-window-spam-score @window-state)]))]
      (is= (spyx-pretty result)
        [[0 [0] 0]
         [1 [0 1] 1/2]
         [2 [0 1 2] 1]
         [3 [1 2 3] 2]
         [4 [2 3 4] 3]
         [5 [3 4 5] 4]
         [6 [4 5 6] 5]
         [7 [5 6 7] 6]]))))

; verify fifo window & window spam limit check
(dotest
  (with-redefs [window-size-max 3
                window-spam-score-max 4]
    (nl) (window-reset!)
    (let [result (forv [ii (thru 7)]
                   (let [new-window (add-email-to-window @window-state {:email-address "x" :spam-score ii})
                         new-spam-score (calc-window-spam-score new-window)]
                     (when (spam-score-ok? new-spam-score)
                       (dosync (ref-set window-state new-window)))
                     [ii (mapv #(grab :spam-score %) @window-state) (calc-window-spam-score @window-state)]))]
      (is= (spyx-pretty result)
        [[0 [0] 0]
         [1 [0 1] 1/2]
         [2 [0 1 2] 1]
         [3 [1 2 3] 2]
         [4 [2 3 4] 3]
         [5 [3 4 5] 4]
         [6 [3 4 5] 4]
         [7 [3 4 5] 4]] ) )))


