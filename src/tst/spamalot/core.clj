(ns tst.spamalot.core
  (:use spamalot.core tupelo.core tupelo.test)
  (:require
    [clojure.string :as str]
    [clojure.pprint :as pprint]
    [clojure.spec.alpha :as sp]
    [clojure.spec.test.alpha :as stest]
    [clojure.spec.gen.alpha :as gen]
    ) )

(def num-emails 5)

(def email-domains
  #{"indeediot.com"
    "monstrous.com"
    "linkedarkpattern.com"
    "dired.com"
    "lice.com"
    "careershiller.com"
    "glassbore.com"})

(def email-regex
  #"^[a-zA-Z0-9._%+-]+@[a-zA-Z0-9.-]+\.[a-zA-Z]{2,63}$")

(sp/def ::email-address
  (sp/with-gen
    (sp/and string? #(re-matches email-regex %))
    #(->>
       (gen/tuple (gen/such-that not-empty (gen/string-alphanumeric))
         (sp/gen email-domains))
       (gen/fmap (fn [[addr domain]] (str addr "@" domain))))))

(sp/def ::spam-score
  (sp/double-in :min 0 :max 1))

(sp/def ::email-record
  (sp/keys :req-un [::email-address ::spam-score]))

(dotest
  (let [email-recs (vec (gen/sample (sp/gen ::email-record) num-emails))
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

    (is (every? truthy? valid-flgs)))

  (nl) (window-reset!)
  (with-redefs [window-size 3]
    (is= (forv [ii (thru 7)]
           (do
             (add-to-window ii)
             [ii (vec @window)]))
      [[0 [0]]
       [1 [0 1]]
       [2 [0 1 2]]
       [3 [1 2 3]]
       [4 [2 3 4]]
       [5 [3 4 5]]
       [6 [4 5 6]]
       [7 [5 6 7]]]))

  )

