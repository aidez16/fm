# Future Movement Daily Summary

A Spring Boot (Java 21) service that reads a fixed-width `PROCESSED FUTURE MOVEMENT` file and generates a daily summary of net transaction quantity for each client and product.

## What it does

For each client and product combination:

`Total = QUANTITY LONG - QUANTITY SHORT`

Output columns:

* `Client_Information`
* `Product_Information`
* `Total_Transaction_Amount`

Example:

```csv
Client_Information,Product_Information,Total_Transaction_Amount
CL-1234-0002-0001,SGX-FU-NK-20100910,-52
CL-1234-0003-0001,CME-FU-N1-20100910,285
CL-1234-0003-0001,CME-FU-NK.-20100910,-215
CL-4321-0002-0001,SGX-FU-NK-20100910,46
CL-4321-0003-0001,CME-FU-N1-20100910,-79
```

The provided `Input.txt` contains 717 transactions, which are grouped into 5 summary rows.

## How to run

The project uses Java 21 and Gradle.

```bash
gradle wrapper --gradle-version 9.7.0
./gradlew bootRun
```

JSON:

```bash
curl http://localhost:8080/api/v1/future-movements/summary
```

CSV:

```bash
curl -OJ http://localhost:8080/api/v1/future-movements/summary/csv
```

Input file:

`src/main/resources/data/Input.txt`

Output file:

`../sample-output/Output.csv`

The input and output paths can be configured in `application.yml`.

## Design

The application is divided into several simple layers:

```text
FixedWidthFileReader
        ↓
FixedWidthLineParser
        ↓
ProcessedFutureMovementMapper
        ↓
FutureMovementIngestionService
        ↓
DailySummaryService
        ↓
REST API / CSV
```

The fixed-width layout is stored in `record-schemas.yml`, instead of hard-coding field positions in the parser.

This makes the parser reusable if another fixed-width record type needs to be supported.

`DailySummaryService` contains the main aggregation logic and groups records by client and product.

`BigDecimal` is used for quantities and totals to avoid calculation errors.

## Kafka

The project also supports Kafka streaming.

```text
Kafka
  ↓
KafkaListener
  ↓
Parser + Mapper
  ↓
DailyAggregateStore
  ↓
REST API
```

For local Kafka:

```bash
docker compose up kafka
KAFKA_BOOTSTRAP_SERVERS=localhost:9092 ./gradlew bootRun
```

The demo can publish the bundled `Input.txt` automatically on startup.

It can also be triggered manually:

```bash
curl -X POST http://localhost:8080/api/v1/future-movements/replay-sample-data
```

## Tests

The project includes unit, integration and end-to-end tests covering:

* Fixed-width parsing
* Field mapping
* Quantity calculation
* Aggregation
* CSV output
* REST APIs
* Kafka producer/consumer
* Error handling
* Full processing of the provided `Input.txt`

Run tests:

```bash
./gradlew test
```

Build:

```bash
./gradlew build
```

## Project structure

```text
src/main/java/com/demo/futuremovement/
├── parser/
├── model/
├── mapper/
├── service/
├── kafka/
├── dto/
├── csv/
├── controller/
└── exception/

src/main/resources/
├── record-schemas.yml
├── application.yml
└── data/Input.txt

sample-output/Output.csv
build.gradle
settings.gradle
Dockerfile
```

The main goal is to keep the parsing reusable, the aggregation logic simple, and the file/Kafka processing paths easy to test and maintain.
