(ns tst.spamalot.core
  (:use spamalot.core tupelo.core tupelo.test)
  (:require
    [clojure.pprint :as pprint]
    [clojure.spec.alpha :as sp]
    [clojure.spec.test.alpha :as stest]
    [clojure.spec.gen.alpha :as gen]
    ))

; validate email generation using spec
(dotest
  (let [email-recs (gen-emails num-emails)
        valid-flgs (forv [email-rec email-recs]
                     (and
                       (contains-key? email-rec :email-address)
                       (contains-key? email-rec :spam-score)))]
    ;(spyx-pretty email-recs) ; sample vvv
    ;[{:email-address "5@monstrous.com", :spam-score 1.0}
    ; {:email-address "I@monstrous.com", :spam-score 0.5}
    ; {:email-address "z1@dired.com", :spam-score 0.875}
    ; {:email-address "2@careershiller.com", :spam-score 1.0}
    ; {:email-address "o9l0@glassbore.com", :spam-score 0.5}]

    (is (every? truthy? valid-flgs))))

; verify recording emails as seen/unseen
(dotest
  (let [dummy-email (fn [addr] {:email-address addr :spam-score 0.1})]
    (email-seen-reset!)
    (isnt (seen-email? (dummy-email "aa")))
    (record-email (dummy-email "aa"))
    (is (seen-email? (dummy-email "aa")))

    (isnt (seen-email? (dummy-email "bb")))
    (record-email (dummy-email "bb"))
    (is (seen-email? (dummy-email "bb")))

    (record-email (dummy-email "cc"))
    (is (every? truthy? (mapv seen-email? (mapv dummy-email ["aa" "bb" "cc"]))))
    (is (every? falsey? (mapv seen-email? (mapv dummy-email ["dd" "xx" "666"]))))))

; verify individual email spam test
(dotest
  (is (non-spammy-email {:email-address "fred@monstrous.com", :spam-score 0.1}))
  (is (non-spammy-email {:email-address "fred@monstrous.com", :spam-score 0.3}))
  (isnt (non-spammy-email {:email-address "fred@monstrous.com", :spam-score 999})) )

; verify cum email stats test
(dotest
  ; cum status works
  (cum-stats-reset!)
  (accumulate-email-stats {:email-address "xxx", :spam-score 1})
  (is= @cum-stats {:cum-spam-score 1.0 :cum-num-emails 1})
  (accumulate-email-stats {:email-address "xxx", :spam-score 2})
  (is= @cum-stats {:cum-spam-score 3.0 :cum-num-emails 2})

  ; verify cum stats + check if new email within cum limits
  (with-redefs [cum-spam-score-max 2]
    (cum-stats-reset!)
    (is (new-email-ok-cum-stats? {:email-address "xxx", :spam-score 1}))
    (is (new-email-ok-cum-stats? {:email-address "xxx", :spam-score 2}))
    (isnt (new-email-ok-cum-stats? {:email-address "xxx", :spam-score 3}))

    (accumulate-email-stats {:email-address "xxx", :spam-score 1})
    (is (new-email-ok-cum-stats? {:email-address "xxx", :spam-score 3}))
    (isnt (new-email-ok-cum-stats? {:email-address "xxx", :spam-score 4}))

    (accumulate-email-stats {:email-address "xxx", :spam-score 3})
    (is= {:cum-spam-score 4.0 :cum-num-emails 2} @cum-stats)
  )
  )

