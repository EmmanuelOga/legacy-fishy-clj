(ns rainbowfish.xmldb-test
  (:require [rainbowfish.xmldb :as xmldb]
            [clojure.test :refer :all]))

(defn setup-database
  [t]
  (xmldb/ensure-running)
  (xmldb/ensure-assets))

(use-fixtures :once setup-database)

;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;
;; Schema tests.
;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;



