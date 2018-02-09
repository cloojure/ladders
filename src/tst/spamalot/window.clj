(ns tst.spamalot.window
  (:use spamalot.window tupelo.core tupelo.test)
  (:require
    [clojure.spec.alpha :as sp]
    [clojure.spec.test.alpha :as stest]
    [clojure.spec.gen.alpha :as gen]))

(dotest
  (with-redefs [window-size-max 3]
    (nl) (window-reset!)
    (pretty (forv [ii (thru 7)]
              (do
                (swap! window-atom add-email-to-window {:email-address "x" :spam-score ii})
                [ii (mapv #(grab :spam-score %) @window-atom) (calc-window-spam-score @window-atom)])))
    #_(is=
      [[0 [0]]
       [1 [0 1]]
       [2 [0 1 2]]
       [3 [1 2 3]]
       [4 [2 3 4]]
       [5 [3 4 5]]
       [6 [4 5 6]]
       [7 [5 6 7]]])))


