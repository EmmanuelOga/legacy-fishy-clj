(ns rainbowfish.file-util-test
  (:require [rainbowfish.file-util :as fu]
            [clojure.test :refer :all]))

(deftest get-base-and-ext
  (is (= [nil nil] (fu/get-base-and-ext "")))
  (is (= [nil nil] (fu/get-base-and-ext ".")))
  (is (= [nil "x"] (fu/get-base-and-ext ".x")))
  (is (= ["c" "x"] (fu/get-base-and-ext "/abc/b/c.x")))
  (is (= ["abc" nil] (fu/get-base-and-ext "abc")))
  (is (= ["abc" nil] (fu/get-base-and-ext "abc.")))
  (is (= ["abc" "x"] (fu/get-base-and-ext "abc.x")))
  (is (= ["abc" "x"] (fu/get-base-and-ext "c:/abc.x")))
  (is (= [nil "x"] (fu/get-base-and-ext "c:/abc/.x")))
  (is (= ["x" "y"] (fu/get-base-and-ext "x.y")))
  (is (= ["index" "topic"] (fu/get-base-and-ext "/index.topic")))
  (is (= ["index" "topic"] (fu/get-base-and-ext "/a/b/c/index.topic"))))

  
