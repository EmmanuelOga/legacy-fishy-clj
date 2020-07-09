(ns rainbowfish.file-util-test
  (:require [rainbowfish.file-util :as fu]
            [clojure.test :refer :all]))

(deftest get-base-name-and-ext
  (is (= [nil nil] (fu/get-base-name-and-ext "")))
  (is (= [nil nil] (fu/get-base-name-and-ext ".")))
  (is (= [nil "x"] (fu/get-base-name-and-ext ".x")))
  (is (= ["c" "x"] (fu/get-base-name-and-ext "/abc/b/c.x")))
  (is (= ["abc" nil] (fu/get-base-name-and-ext "abc")))
  (is (= ["abc" nil] (fu/get-base-name-and-ext "abc.")))
  (is (= ["abc" "x"] (fu/get-base-name-and-ext "abc.x")))
  (is (= ["abc" "x"] (fu/get-base-name-and-ext "c:/abc.x")))
  (is (= [nil "x"] (fu/get-base-name-and-ext "c:/abc/.x")))
  (is (= ["x" "y"] (fu/get-base-name-and-ext "x.y")))
  (is (= ["index" "topic"] (fu/get-base-name-and-ext "/index.topic")))
  (is (= ["index" "topic"] (fu/get-base-name-and-ext "/a/b/c/index.topic"))))

  
