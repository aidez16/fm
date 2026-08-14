# Future Movement Daily Summary

A full-stack solution for System A's fixed-width `PROCESSED FUTURE MOVEMENT` feed.

The project includes a Spring Boot backend for parsing and aggregation, an Angular 22 frontend, Kafka streaming ingestion, and Docker/Kubernetes deployment.

```text
├── backend/            Spring Boot (Java 21, Gradle)
├── frontend/           Angular 22 + Angular Material 3
├── docker-compose.yml  Kafka + backend + frontend
├── k8s/                Kubernetes manifests
└── sample-output/      Output.csv
```

See [backend/README.md](backend/README.md) and [frontend/README.md](frontend/README.md) for more details.

## Quick Start

```bash
docker compose up --build
```

Then open **http://localhost:4200**.

This starts Kafka, the Spring Boot backend and the Angular frontend. The backend loads the sample `Input.txt` into Kafka and the frontend displays the summary.

To replay the sample data:

```bash
curl -X POST http://localhost:8080/api/v1/future-movements/replay-sample-data
```

The replay resets the aggregate before processing the data, so the result stays the same.

## What It Does

For each unique **(client, product)** combination, the application calculates:

```text
Net Total = QUANTITY_LONG - QUANTITY_SHORT
```

The output contains:

| Column                     | Description                                         |
| -------------------------- | --------------------------------------------------- |
| `Client_Information`       | Client type, number, account and subaccount         |
| `Product_Information`      | Exchange, product group, symbol and expiration date |
| `Total_Transaction_Amount` | Net transaction quantity                            |

The sample file contains 717 transactions and produces 5 summary rows.

The report is written to `sample-output/Output.csv` and is also available through the CSV download endpoint.

## Streaming

The backend consumes fixed-width transaction lines from the Kafka topic:

```text
future-movements
```

A `@KafkaListener` processes each message and updates the in-memory daily aggregate.

The same parsing and aggregation logic is used for both file and Kafka processing.

## Running Without Docker

Start Kafka:

```bash
docker compose up kafka
```

Backend:

```bash
cd backend
KAFKA_BOOTSTRAP_SERVERS=localhost:9092 ./gradlew bootRun
```

Frontend:

```bash
cd frontend
npm install
npm start
```

Open **http://localhost:4200**.

## Kubernetes

Build and push the images:

```bash
docker build -t your-registry/future-movement-backend:1.0.0 ./backend
docker build -t your-registry/future-movement-frontend:1.0.0 ./frontend

docker push your-registry/future-movement-backend:1.0.0
docker push your-registry/future-movement-frontend:1.0.0
```

Update the `image:` fields in the Kubernetes manifests, then:

```bash
kubectl apply -f k8s/00-namespace.yaml
kubectl apply -f k8s/01-kafka.yaml
kubectl apply -f k8s/02-backend.yaml
kubectl apply -f k8s/03-frontend.yaml
kubectl apply -f k8s/04-ingress.yaml
```

For local access:

```bash
kubectl -n future-movement-summary port-forward svc/frontend 4200:80
```

The Kafka deployment is intended for local/demo use.
