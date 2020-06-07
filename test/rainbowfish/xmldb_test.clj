(ns rainbowfish.xmldb-test
  (:require [rainbowfish.xmldb :as xmldb]
            [clojure.test :refer :all]
            [clojure.string :as str]))

(defn setup-database
  [t]
  (xmldb/ensure-running)
  (xmldb/ensure-assets))

(use-fixtures :once setup-database)

;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;
;; Schema tests.
;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;

(defn without-errors
  "This simplistic check will just check that the string 'invalid' is
  not present for now."
  [xml-str]
  (nil? (str/index-of xml-str "invalid")))

(deftest it-passes-validation-on-fixtures
  (let [result (xmldb/fire
                "OPEN rainbowfish"
                (str "RUN " (xmldb/assets-path) "/assets/tests/validate-fixtures.xq"))]
    (is (without-errors result))))




