# Integration-event extraction follow-up

The outbox refactor gives each module an independent relay and scheduler. It
continues to use JPA rows that implement the shared `OutboxEntry` contract, and
it delivers shared integration-event types through Spring's in-process event
publisher.

- Publish versioned event contracts from artifacts owned by the producing
  modules. Migrate consumers from `app/shared/events/<owner>` to those artifacts.
- Implement a broker-backed `OutboxEventDispatcher` that preserves the event ID,
  version, and trace headers. Treat broker acknowledgement as the publish outcome
  and retain the existing retry and claim fencing semantics.
- Verify mixed-version compatibility, redelivery through the existing consumer
  inboxes, and producer/consumer operation in separate processes before moving
  any live workloads off in-process dispatch.
