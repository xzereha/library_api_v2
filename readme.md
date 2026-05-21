# Library API

[![Build and Test](https://github.com/xzereha/library_api_v2/actions/workflows/test.yml/badge.svg)](https://github.com/xzereha/library_api_v2/actions/workflows/test.yml)
[![Style Check](https://github.com/xzereha/library_api_v2/actions/workflows/style.yml/badge.svg)](https://github.com/xzereha/library_api_v2/actions/workflows/style.yml)

## Dependencies

| Dependency | Required | Notes |
|---|---|---|
| Java 21 | Yes | |
| Docker | Only for Redis | Redis runs in a container; install Docker if you need caching |
| Redis (via Docker) | Optional | `docker run -d --name redis -p 6379:6379 redis:7-alpine` |
| Vault | Optional | Falls back gracefully with `optional:vault://` import |

The following are provided at runtime and need no installation:
- **H2** (in-memory database)
- **Swagger UI** (at `/swagger-ui.html`)
- **SpringDoc OpenAPI** (at `/api-docs`)

## Environment Variables

| Variable | Default | Required | Description |
|---|---|---|---|
| `VAULT_ADDR` | `http://localhost:8200` | Only if Vault is running | HashiCorp Vault address |
| `VAULT_TOKEN` | — | Only if Vault is running | Vault authentication token |
| `SPRING_REDIS_HOST` | `localhost` | No | Redis server hostname |
| `SPRING_REDIS_PORT` | `6379` | No | Redis server port |

### Vault (optional)

If you want to run Vault for secrets management:

```bash
vault server -dev -dev-root-token-id="root"
export VAULT_ADDR=http://localhost:8200
export VAULT_TOKEN="root"
vault kv put secret/library_api secret="your_secret_value"
```

Vault is configured as `optional:vault://` in `application.yaml`, so the app starts fine without it.

### Redis (required for caching, optional otherwise)

Start Redis via Docker:

```bash
docker run -d --name redis -p 6379:6379 redis:7-alpine
```

## Running Without Redis

The app defaults to Redis for caching. If you don't have Redis running, disable it at startup:

### Option A — Disable caching entirely
```bash
./gradlew bootRun --args='--spring.cache.type=none'
```

### Option B — Use in-memory cache (for benchmarking comparisons)
```bash
./gradlew bootRun --args='--spring.cache.type=simple'
```

This uses Spring's `ConcurrentHashMap`-based cache with no external dependencies.

### Option C — Set env var and run normally
```bash
SPRING_CACHE_TYPE=none ./gradlew bootRun
```

## Build

```bash
./gradlew build
```

## Run

With Redis:
```bash
./gradlew bootRun
```

Without Redis:
```bash
./gradlew bootRun --args='--spring.cache.type=none'
```

## Test

```bash
./gradlew test
```

## Benchmarks

Benchmarks were run with [ab](https://httpd.apache.org/docs/2.4/programs/ab.html) against `GET /api/v1/books` (1000 books, 100 concurrent clients, 1M requests total).

| Metric | No Cache | Redis Cache | Spring Cache | Redis vs No Cache | Spring vs No Cache |
|---|---|---|---|---|---|
| Requests/sec | 12,481.33 | 44,340.37 | 50,320.19 | **+255%** | **+303%** |
| Mean latency | 8.012 ms | 2.255 ms | 1.987 ms | **−71.9%** | **−75.2%** |
| P50 latency | 8 ms | 2 ms | 2 ms | −75% | −75% |
| P99 latency | 16 ms | 4 ms | 3 ms | −75% | −81.3% |

Both caching strategies offer massive improvements over no cache. The in-memory Spring cache (ConcurrentHashMap) has a slight edge over Redis due to the absence of network round-trips — expect ~13-18% more throughput with Spring cache vs Redis cache.

Raw results are in the [`benchmark/`](benchmark/) directory.

## API Docs

Once running, visit:
- Swagger UI: [http://localhost:8080/swagger-ui.html](http://localhost:8080/swagger-ui.html)
- OpenAPI spec: [http://localhost:8080/api-docs](http://localhost:8080/api-docs)
- H2 Console: [http://localhost:8080/h2-console](http://localhost:8080/h2-console)
