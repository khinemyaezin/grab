# RMI adapter

`rmi-adapter` provides opt-in service-to-service communication for the published RMI message contracts.

## Provider

Import `RmiProviderConfiguration` and set `grab.rpc.enabled=true`. Mark each contract implementation with `@RmiExport`:

```java
@Component
@RmiExport
public class MerchandisingPortAdapter implements MerchandisingPort {
}
```

The service owns one registry endpoint. Every marked contract is bound on the configured registry port using its fully qualified interface name:

```yaml
grab:
  rpc:
    enabled: true
    registry-port: 1100
    bind-address: 0.0.0.0
    advertised-host: merchandising-service
    connect-timeout-ms: 3000
    read-timeout-ms: 30000
```

`advertised-host` is the host returned in exported stubs. The timeout values are applied by the provider socket factory and govern remote invocations.

## Consumer

Import `RmiClientConfiguration`, inject `RmiClientFactory`, and declare a typed bean for the published contract:

```java
@Bean
public MerchandisingPort merchandisingPort(RmiClientFactory factory) {
    return factory.create(MerchandisingPort.class, "merchandising");
}
```

Configure the provider endpoint under the service key. Client lookup is lazy, so the consumer can start before the provider. A failed lookup or invocation is returned as `RemoteException` and clears the cached stub so the next call can reconnect:

```yaml
grab:
  rpc:
    clients:
      merchandising:
        host: merchandising-service
        port: 1100
```

Client-specific `connect-timeout-ms` and `read-timeout-ms` override the global `grab.rpc` values. The adapter does not retry a failed invocation.
