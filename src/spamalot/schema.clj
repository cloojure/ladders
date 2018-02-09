(ns spamalot.schema
  (:use tupelo.core)
  (:require
    [schema.core :as s]
    [tupelo.schema :as tsk]
  ))

(def Email {:email-address s/Str
            :spam-score    s/Num})

(def CumStats {:cum-spam-score s/Num
               :cum-num-emails s/Int})
