# testa

A small testing utility library that helps you write `clojure.test` tests for async behavior in Clojure. It lets you write async tests, aka, tests for async code.

## Installation

Remember, this library is meant to be used in your tests, so you probably want to add it into your test dependencies, not as a dependency of your app/lib.

### Leiningen

Add the following dependency to your `project.clj`:

```clojure
[com.xadecimal/testa "0.1.0"]
```

### Clojure CLI/deps.edn

Add the following dependency to your `deps.edn`:

```clojure
{:deps {com.xadecimal/testa {:mvn/version "0.1.0"}}}
```

## Usage

Testa provides utilities to enqueue and dequeue values during async tests and a macro to scope these operations within individual tests.

### Functions

#### `q!`

Enqueue a value into the bound queue `*q*`. Use it in async tests to queue up values you want to assert later.

```clojure
(q! x)
```

#### `dq!`

Dequeue from the bound queue `*q*`. Waits for a specified timeout (default 1000ms) for a value to show up in the queue. Returns `:timeout` if no value appears within the timeout period.

```clojure
(dq!)
(dq! 5000)
```

### Macro

#### `testa`

Similar to `clojure.test`'s `testing` macro, but binds `*q*` to a new queue for use within the test body. Use it for async tests.

```clojure
(testa
  "description"
  test-body...)
```

## Examples

Here are some simple example tests written with testa:

```clojure
(ns some-test-ns
  (:require [clojure.test :refer [deftest is]]
            [com.xadecimal.testa :refer [dq! q! testa]]))

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

(deftest testing-an-async-fn-timeout
  (testa "Async code takes too long fetching results"
         (future
           ;; Imagine this fetches from an API
           (Thread/sleep 100)
           ;; Imagine this is the result from the fetch
           (q! :pretend-result))
         ;; We want to assert the async code does something in under 200ms and
         ;; that it does the correct thing.
         (is (= :pretend-result (dq! 200)))))
```

In the first test, we simulate asynchronous fetch operations with different delays to represent fetching from APIs with different response times. We use `q!` to queue the results and then assert that they are dequeued in the correct order. In the second test, we verify that an asynchronous operation completes within a specified timeout and produces the expected result.

## License

This project is licensed under the MIT License. See the `LICENSE` file for details.
