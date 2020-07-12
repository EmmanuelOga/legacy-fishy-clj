(ns rainbowfish.http-app-test
  (:require [rainbowfish.http-app :as ha]
            [clojure.test :refer :all]))

(defn mock-req
  [request-path]
  {:request-path request-path :canonical "https://cannonical.host"})

(deftest request-to-topic
  (is (= nil (ha/request-to-topic (mock-req "/a.unknown-ext"))))

  (is (= {:topic-name "/index.topic"
          :topic-graph "https://cannonical.host/index.topic"
          :topic-content-type "text/html"}
         (ha/request-to-topic (mock-req "/index"))))

  (is (= {:topic-name "/index.topic"
          :topic-graph "https://cannonical.host/index.topic"
          :topic-content-type "text/html"}
         (ha/request-to-topic (mock-req "/index.html"))))

  (is (= {:topic-name "/index.topic"
          :topic-graph "https://cannonical.host/index.topic"
          :topic-content-type "application/xml"}
         (ha/request-to-topic (mock-req "/index.topic"))))

  (is (= {:topic-name "/index.topic"
          :topic-graph "https://cannonical.host/index.topic"
          :topic-content-type "text/turtle"}
         (ha/request-to-topic (mock-req "/index.ttl"))))

  ;; ---------------------------------------------------------------------------

  (is (= {:topic-name "/a/index.topic"
          :topic-graph "https://cannonical.host/a/index.topic"
          :topic-content-type "text/html"}
         (ha/request-to-topic (mock-req "/a/index"))))

  (is (= {:topic-name "/a/index.topic"
          :topic-graph "https://cannonical.host/a/index.topic"
          :topic-content-type "text/html"}
         (ha/request-to-topic (mock-req "/a/index.html"))))

  (is (= {:topic-name "/a/index.topic"
          :topic-graph "https://cannonical.host/a/index.topic"
          :topic-content-type "application/xml"}
         (ha/request-to-topic (mock-req "/a/index.topic"))))

  (is (= {:topic-name "/a/index.topic"
          :topic-graph "https://cannonical.host/a/index.topic"
          :topic-content-type "text/turtle"}
         (ha/request-to-topic (mock-req "/a/index.ttl"))))

  ;; ---------------------------------------------------------------------------

  (is (= {:topic-name "/my/nice/topic/something-very-nice-99.topic"
          :topic-graph "https://cannonical.host/my/nice/topic/something-very-nice-99.topic"
          :topic-content-type "text/html"}
         (ha/request-to-topic (mock-req "/my/nice/topic/something-very-nice-99"))))

  (is (= {:topic-name "/my/nice/topic/something-very-nice-99.topic"
          :topic-graph "https://cannonical.host/my/nice/topic/something-very-nice-99.topic"
          :topic-content-type "text/html"}
         (ha/request-to-topic (mock-req "/my/nice/topic/something-very-nice-99.html"))))

  (is (= {:topic-name "/my/nice/topic/something-very-nice-99.topic"
          :topic-graph "https://cannonical.host/my/nice/topic/something-very-nice-99.topic"
          :topic-content-type "application/xml"}
         (ha/request-to-topic (mock-req "/my/nice/topic/something-very-nice-99.topic"))))

  (is (= {:topic-name "/my/nice/topic/something-very-nice-99.topic"
          :topic-graph "https://cannonical.host/my/nice/topic/something-very-nice-99.topic"
          :topic-content-type "text/turtle"}
         (ha/request-to-topic (mock-req "/my/nice/topic/something-very-nice-99.ttl"))))
  )

