(ns com.xadecimal.testa-test
  (:require [clojure.test :refer [deftest is]]
            [com.xadecimal.testa :refer [dq! q! testa]]))

(deftest simple-enqueue-dequeue-test
  (testa "Simple enqueue and dequeue test"
         (q! 1)
         (q! 2)
         (is (= 1 (dq!)))
         (is (= 2 (dq!)))))

(deftest dequeue-timeout-test
  (testa "Dequeue with timeout"
         (is (= :timeout (dq! 500)))
         (q! 3)
         (is (= 3 (dq!)))))

(deftest enqueue-in-async-code
  (testa "Enqueue in async code succeed when under timeout"
         (future (Thread/sleep 500)
                 (q! :async-result))
         (is (= :async-result (dq!))))
  (testa "Enqueue in async code fails when over timeout"
         (future (Thread/sleep 500)
                 (q! :async-result))
         (is (= :timeout (dq! 100)))))

(deftest testing-an-async-fn
  (testa "Async code fetches results as expected"
         (future
           ;; Imagine this fetches from a slow API
           (Thread/sleep 500)
           ;; Imagine this is the result from the fetch
           (q! :result1))
         (future
           ;; Imagine this fetches from a fast API
           (Thread/sleep 100)
           ;; Imagine this is the result from the fetch
           (q! :result2))
         ;; We want to assert each async code got the expected result, and
         ;; that they got it in the order we expected
         (is (= :result2 (dq!)))
         (is (= :result1 (dq!)))))

(deftest testing-an-async-fn
  (testa "Async code takes too long fetching results"
         (future
           ;; Imagine this fetches from an API
           (Thread/sleep 100)
           ;; Imagine this is the result from the fetch
           (q! :pretend-result))
         ;; We want to assert the async code does something in under 200ms and
         ;; that it does the correct thing.
         (is (= :pretend-result (dq! 200)))))
