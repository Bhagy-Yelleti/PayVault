# PayVault — Fault-Tolerant Digital Wallet & Real-Time Financial Ledger System

PS020 · 24SDCS03R — SOA Programming and Microservices

This is the full working system: Eureka Server, API Gateway, User Service, Wallet
Service, Transaction Service, and a simple frontend — matching the architecture we
designed together.

---

## 1. What you need to install first (one-time setup)

You need these three things on your computer before anything will run:

1. **Java 17 (JDK)** — download from [adoptium.net](https://adoptium.net/) if you don't
   have it. Check with `java -version` in a terminal — it should say 17 or higher.
2. **Maven** — most IDEs (IntelliJ, Eclipse, VS Code with Java extension) come with
   Maven built in, so you usually don't need to install it separately. If you're using
   a terminal directly, install it from [maven.apache.org](https://maven.apache.org/).
3. **PostgreSQL Server** — download from
   [postgresql.org/download](https://www.postgresql.org/download/) and install it.
   During setup it will ask you to set a password for the `postgres` user — this
   project assumes you set it to `root` to match the config exactly. If you used a
   different password, edit the config in step 2 below.

**You DO need to manually create 3 databases** (Postgres, unlike MySQL, can't
auto-create a database from a connection string). This takes 30 seconds — see step 2.
Tables inside those databases are still created automatically by Hibernate on first
startup, so you never need to write any CREATE TABLE statements yourself.

---

## 2. Create the 3 databases (one-time, ~30 seconds)

Open a terminal and run:

```bash
psql -U postgres
```

Enter your Postgres password when prompted (`root`, if you matched the default).
Then paste these three lines and press Enter:

```sql
CREATE DATABASE user_db;
CREATE DATABASE wallet_db;
CREATE DATABASE ledger_db;
```

Type `\q` to exit. That's it — no tables to create by hand, Hibernate creates those
automatically the first time each service starts.

**Prefer a GUI?** If you installed pgAdmin alongside Postgres, you can do the same
thing by right-clicking "Databases" in the left panel → Create → Database, and
creating one each named `user_db`, `wallet_db`, `ledger_db`.

**Config check:** if your Postgres password isn't `root`, or Postgres isn't running
on the default port 5432, edit the `username`/`password`/`url` under `datasource` in
these three files to match:

- `user-service/src/main/resources/application.yml`
- `wallet-service/src/main/resources/application.yml`
- `transaction-service/src/main/resources/application.yml`

---

## 3. Import the projects

Each of these 5 folders is an **independent Spring Boot Maven project**:

```
eureka-server/
api-gateway/
user-service/
wallet-service/
transaction-service/
```

In your IDE (IntelliJ IDEA is easiest): **File → Open**, and open each folder
separately (IntelliJ will detect the `pom.xml` and import it as a Maven project
automatically). Do this for all 5 — you'll end up with 5 separate project windows/modules.

The first time you open each one, your IDE will download all the dependencies listed
in `pom.xml` — this needs an internet connection and can take a few minutes per project.

---

## 4. Run order (this matters!)

Services must start in this order, waiting a bit between each:

| Order | Service | Port | Wait for... |
|---|---|---|---|
| 1 | **eureka-server** | 8761 | Open `http://localhost:8761` in a browser — you should see the Eureka dashboard |
| 2 | **user-service** | 8081 | Console log ends with `Started UserServiceApplication` |
| 3 | **wallet-service** | 8082 | Console log ends with `Started WalletServiceApplication` |
| 4 | **transaction-service** | 8083 | Console log ends with `Started TransactionServiceApplication` |
| 5 | **api-gateway** | 8080 | Console log ends with `Started ApiGatewayApplication` |

To run each one: open its `...Application.java` file (e.g. `EurekaServerApplication.java`)
in your IDE and click the green ▶ Run button. Wait for each to fully start (check the
console) before starting the next.

**How to know it's working:** after all 5 are running, refresh
`http://localhost:8761` — you should see `USER-SERVICE`, `WALLET-SERVICE`,
`TRANSACTION-SERVICE`, and `API-GATEWAY` all listed as registered instances.

---

## 5. Run the frontend

The frontend is a single static file — no build step, no npm needed.

Just **double-click `frontend/index.html`** to open it in your browser, or right-click
→ "Open with" → your browser. It talks to the gateway at `http://localhost:8080`.

Try the full flow: Register → verify the OTP (it's pre-filled on screen since there's
no real SMS in dev mode) → set a PIN → you land on the dashboard → Add Money → Send
Money to another registered user (you'll need their numeric user ID — register a
second account to test transfers between two users).

---

## 6. Testing the backend directly (optional, without the frontend)

You can also test with curl or Postman, straight through the gateway on port 8080:

```bash
# Register
curl -X POST http://localhost:8080/api/users/register \
  -H "Content-Type: application/json" \
  -d '{"name":"Priya Reddy","email":"priya@test.com","phone":"9876543210","password":"pass123"}'

# Login (grab the token from the response)
curl -X POST http://localhost:8080/api/users/login \
  -H "Content-Type: application/json" \
  -d '{"email":"priya@test.com","password":"pass123"}'

# Check balance (replace <TOKEN> and <USER_ID>)
curl http://localhost:8080/api/wallets/<USER_ID>/balance \
  -H "Authorization: Bearer <TOKEN>"
```

---

## 7. What's implemented vs. what's a stretch goal

**Fully implemented and working:**
- JWT authentication, OTP verification, transaction PIN (User Service)
- Wallet balance, credit, debit, top-up with optimistic locking (Wallet Service)
- P2P transfer saga: validate → PIN check → debit → credit → ledger, with
  compensating rollback on failure (Transaction Service)
- Idempotency keys (duplicate transfer requests are rejected safely)
- Resilience4j circuit breaker + retry on every inter-service call
- Eureka service discovery + API Gateway routing + a global JWT filter at the gateway
- Transaction history and category-based spending summary

**Listed as extra-credit ideas in our architecture doc but not built here** (you can
add these yourself for more marks, or ask me to build any of them next):
- Kafka/RabbitMQ async notifications
- Scheduled reconciliation job
- Rate limiting at the gateway
- Distributed tracing (Zipkin)
- Fraud detection rules

---

## 8. Deploying with Docker (containerized — the "deployment-ready" part of the rubric)

This is a separate, self-contained way to run the whole system — it spins up its own
Postgres containers (on different host ports so it won't clash with the Postgres you
installed for local IDE testing) and doesn't touch anything from sections 1-7.

**Prerequisite:** install Docker Desktop from [docker.com](https://www.docker.com/products/docker-desktop/)
and make sure it's running (you'll see its icon in your system tray/menu bar).

From a terminal, `cd` into the `payvault-microservices` folder (the one with
`docker-compose.yml` in it), then run:

```bash
docker-compose up --build
```

First run takes a few minutes (downloading base images + building each service).
You'll see interleaved logs from all 8 containers (3 databases + 5 services) in one
terminal. Wait until you stop seeing new startup logs — `api-gateway` finishing its
`Started ApiGatewayApplication` line means everything is up.

**Confirm it worked:** open `http://localhost:8761` — same as before, you should see
all 4 services registered.

**Use it exactly the same way:** the frontend (`frontend/index.html`) and all the curl
examples from section 6 work identically — the gateway is still on port 8080.

**Stop everything:**
```bash
docker-compose down
```

**Stop and wipe all data (start completely fresh next time):**
```bash
docker-compose down -v
```

**If a service fails to start on the first attempt** (e.g. it tried to connect to its
database before Postgres was fully ready) — the `restart: on-failure` policy means it
retries automatically, but if it's still stuck after ~30 seconds, just run
`docker-compose up --build` again; Docker reuses what already built successfully.

---

## 9. Common problems

- **"Connection refused" on port 5432** → Postgres isn't running. Start it from
  Services (Windows), `brew services start postgresql` (Mac), or
  `sudo systemctl start postgresql` (Linux).
- **"database ... does not exist"** → you skipped step 2 — go create the 3 databases
  with `psql -U postgres` as described above, then restart the service.
- **"password authentication failed for user postgres"** → the password in
  `application.yml` doesn't match what you set for the `postgres` user during
  install. Either reset it to `root`, or update all three `application.yml` files
  with your actual password.
- **A service starts but doesn't show up in Eureka** → wait ~30 seconds; Eureka
  registration isn't instant. If it's still missing, check that service's console
  log for errors connecting to `localhost:8761`.
- **"Port already in use"** → something else is using that port, or you started the
  same service twice. Stop the other process or change the port in that service's
  `application.yml`.
- **Frontend shows a CORS or network error** → make sure api-gateway is actually
  running on port 8080 and that you registered/logged in before trying dashboard
  actions.

---

## 10. Project structure

```
payvault-microservices/
├── eureka-server/         Service registry (port 8761)
├── api-gateway/            Routing + JWT filter (port 8080)
├── user-service/           Auth, OTP, PIN, profile (port 8081)
├── wallet-service/         Balance, credit/debit, top-up (port 8082)
├── transaction-service/    Transfer saga, ledger, history (port 8083)
├── frontend/index.html     Static demo client
├── sql/reference_schema.sql  Schema reference (not required to run)
└── README.md               This file
```

Each service's package structure follows the same pattern:
`entity/` → `repository/` → `service/` → `controller/`, with `dto/`, `security/`,
and `exception/` alongside. `transaction-service` additionally has `client/` (Feign
clients to User and Wallet Service).
