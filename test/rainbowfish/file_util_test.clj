(ns rainbowfish.file-util-test
  (:require [clojure.test :refer :all]
            [rainbowfish.file-util :as fu]))

(deftest get-base-name-and-ext
  (is (= [nil nil nil] (fu/get-base-name-and-ext "")))
  (is (= [nil nil nil] (fu/get-base-name-and-ext ".")))
  (is (= [nil nil "x"] (fu/get-base-name-and-ext ".x")))
  (is (= ["/abc/b" "c" "x"] (fu/get-base-name-and-ext "/abc/b/c.x")))
  (is (= [nil "abc" nil] (fu/get-base-name-and-ext "abc")))
  (is (= [nil "abc" nil] (fu/get-base-name-and-ext "abc.")))
  (is (= [nil "abc" "x"] (fu/get-base-name-and-ext "abc.x")))
  (is (= ["c:" "abc" "x"] (fu/get-base-name-and-ext "c:/abc.x")))
  (is (= ["c:/abc" nil "x"] (fu/get-base-name-and-ext "c:/abc/.x")))
  (is (= [nil "x" "y"] (fu/get-base-name-and-ext "x.y")))
  (is (= [nil "index" "topic"] (fu/get-base-name-and-ext "/index.topic")))
  (is (= ["/a/b/c" "index" "topic"] (fu/get-base-name-and-ext "/a/b/c/index.topic"))))

(deftest join
  (is (= nil (fu/join nil nil)))
  (is (= "a" (fu/join "a" nil)))
  (is (= "b" (fu/join nil "b")))
  (is (= "a/b" (fu/join "a" "b")))
  (is (= "a/b" (fu/join "a/" "b")))
  (is (= "a/b" (fu/join "a" "/b")))
  (is (= "a/b" (fu/join "a/" "/b"))))
