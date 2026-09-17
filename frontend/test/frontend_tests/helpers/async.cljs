;; This Source Code Form is subject to the terms of the Mozilla Public
;; License, v. 2.0. If a copy of the MPL was not distributed with this
;; file, You can obtain one at http://mozilla.org/MPL/2.0/.
;;
;; Copyright (c) KALEIDOS SUBSIDIARY SL

(ns frontend-tests.helpers.async
  "Async-first plumbing for ClojureScript tests.

  Lets a test read sequentially while presupposing asynchronous APIs:
  triggers run, effects are awaited as promises, and `done` always runs
  at the end. Relies on two platform properties:

  - `cljs.test` keeps its env in a `set!` var, so assertions inside
    deferred ticks are still counted.
  - RxJS delivers `observe-on :async` notifications in scheduling (FIFO)
    order, so awaiting the last scheduled delivery observes every effect
    scheduled before it."
  (:require
   [beicon.v2.core :as rx]
   [cljs.test :as t :include-macros true]))

(defn ->promise
  "Coerces a single-value observable into a js/Promise that resolves
  with its first value (or rejects on error)."
  [ob]
  (js/Promise. (fn [resolve reject] (rx/subs! resolve reject ob))))

(defn ^:async await-response
  "Pushes `value` into the `response` subject and resolves once the last
  recorded request completes, delivery included.

  The promise subscribes BEFORE pushing: subscribing after the push
  would never resolve. Request entries are maps holding the request
  observable under `:req`."
  [requests response value]
  (let [p (->promise (:req (last @requests)))]
    (rx/push! response value)
    (await p)))

(defn settle
  "Resolves on the next macrotask, letting scheduled deliveries land.
  Await it before asserting absence: silence is only meaningful once the
  queue had a chance to deliver."
  []
  (js/Promise. (fn [resolve] (js/setTimeout resolve 0))))

(defn ^:async wait-for
  "Resolves once `pred` holds, checking immediately and then once per
  macrotask (bounded: records a failure instead of hanging). Await it
  before asserting effects of triggers — it holds whether the effect
  lands synchronously or not."
  [pred msg & [{:keys [max-ticks] :or {max-ticks 50}}]]
  (if (pred)
    nil
    (if (<= max-ticks 0)
      (t/is false (str "Timed out waiting for: " msg))
      (do (await (settle))
          (await (wait-for pred msg {:max-ticks (dec max-ticks)}))))))
