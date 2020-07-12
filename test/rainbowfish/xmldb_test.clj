(ns rainbowfish.xmldb-test
  (:require [clojure.java.io :as io]
            [clojure.string :as str]
            [clojure.test :refer :all]
            [rainbowfish.file-util :as fu]
            [rainbowfish.xmldb :as xmldb]))

(defn setup-database
  []
  (xmldb/ensure-running))

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
  (is (without-errors
       (xmldb/query
        (slurp (io/resource "assets/tests/validate-fixtures.xq"))
        [["$schema" (slurp (io/resource "assets/schemas/sdoc.rnc"))]
         ["$fixtures-path" (fu/relpath "." "resources/assets/tests/fixtures")]]))))
