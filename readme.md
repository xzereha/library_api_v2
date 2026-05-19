# Library API

[![Build and Test](https://github.com/xzereha/library_api_v2/actions/workflows/test.yml/badge.svg)](https://github.com/xzereha/library_api_v2/actions/workflows/test.yml)
[![Style Check](https://github.com/xzereha/library_api_v2/actions/workflows/style.yml/badge.svg)](https://github.com/xzereha/library_api_v2/actions/workflows/style.yml)

## Prerequisites

- Java 21

### Vault

Configure Vault with the following commands to create a dummy secret for the application:

```bash
vault server -dev -dev-root-token-id="root"
vault kv put secret/library_api secret="your_secret_value"
```

Set the `VAULT_TOKEN` environment variable to the root token to allow the application to access Vault:

```bash
export VAULT_TOKEN="root"
```

## Build

```bash
./gradlew build
```

## Run

```bash
./gradlew bootRun
```

## Test

```bash
./gradlew test
```
