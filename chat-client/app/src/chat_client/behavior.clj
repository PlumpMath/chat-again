(ns ^:shared chat-client.behavior
    (:require [clojure.string :as string]
              [io.pedestal.app :as app]
              [io.pedestal.app.util.log :as log]
              [io.pedestal.app.util.platform :as platform]
              [io.pedestal.app.messages :as msg]))
;; While creating new behavior, write tests to confirm that it is
;; correct. For examples of various kinds of tests, see
;; test/chat_client/test/behavior.clj.

(defn set-value-transform [old-value message]
  (:value message))

(defn send-message-transform [sent-messages message]
  (let [max-id ((fnil inc 0) (:max-id sent-messages))]
    (assoc sent-messages
           max-id {:text (:message message)
                   :from "me@papill0n.org"
                   :date (platform/date)}
           :max-id max-id)))

(defn send-message [{message :message}]
  (log/info message))

(defn init-messages [_]
  [[:node-create [:chat] :map]
   [:node-create [:chat :messages] :map]
   [:transform-enable [:chat :messages]
    :send-message [{msg/topic [:chat :messages] (msg/param :message) {}}]]])

(def example-app
  ;; There are currently 2 versions (formats) for dataflow
  ;; description: the original version (version 1) and the current
  ;; version (version 2). If the version is not specified, the
  ;; description will be assumed to be version 1 and an attempt
  ;; will be made to convert it to version 2.
  {:version 2
   :transform [[:set-value [:greeting] set-value-transform]
               [:send-message [:chat :messages] send-message-transform]]
   ; will be sent using the fn configured by app/consume-effects
   :effect #{[{[:chat :messages :*] :message} send-message]}
   :emit [{:init (fn [_] [[:node-create [:greeting] :map]])}
          [#{[:greeting]} (app/default-emitter [])]

          {:init init-messages}
          [#{[:chat :messages :*]} (app/default-emitter [])]]})

;; Once this behavior works, run the Data UI and record
;; rendering data which can be used while working on a custom
;; renderer. Rendering involves making a template:
;;
;; app/templates/chat-client.html
;;
;; slicing the template into pieces you can use:
;;
;; app/src/chat_client/html_templates.cljs
;;
;; and then writing the rendering code:
;;
;; app/src/chat_client/rendering.cljs

(comment
  ;; The examples below show the signature of each type of function
  ;; that is used to build a behavior dataflow.

  ;; transform

  (defn example-transform [old-state message]
    ;; returns new state
    )

  ;; derive

  (defn example-derive [old-state inputs]
    ;; returns new state
    )

  ;; emit

  (defn example-emit [inputs]
    ;; returns rendering deltas
    )

  ;; effect

  (defn example-effect [inputs]
    ;; returns a vector of messages which effect the outside world
    )

  ;; continue

  (defn example-continue [iniputs]
    ;; returns a vector of messages which will be processed as part of
    ;; the same dataflow transaction
    )

  ;; dataflow description reference

  {:transform [[:op [:path] example-transform]]
   :derive    #{[#{[:in]} [:path] example-derive]}
   :effect    #{[#{[:in]} example-effect]}
   :continue  #{[#{[:in]} example-continue]}
   :emit      [[#{[:in]} example-emit]]}
)
