# Future Movement Daily Summary — UI

Angular 22 + Angular Material frontend for the backend summary API.

## Running

```bash
npm install
npm start
```

Open `http://localhost:4200`.

Make sure the backend is running:

```bash
cd ../backend
./gradlew bootRun
```

Other commands:

```bash
npm test
npm run build:prod
```

## Features

* Angular 22 standalone components with zoneless change detection
* Angular Material 3 with light/dark/system theme
* Signal-based `SummaryStore`
* Dashboard cards for client/product counts and net total
* Material table with sorting and text filtering
* CSV download
* Backend API integration through `FutureMovementApiService`

## Project Structure

```text
src/app/
  core/
    models/
    services/
    state/
  shared/components/
    stat-card/
    toolbar/
  features/summary/
    summary-page.component.*
  app.component.ts
  app.config.ts
  app.routes.ts
```

## Kafka Demo

The backend supports Kafka ingestion and provides a replay endpoint for sample data.

The UI already has:

```text
FutureMovementApiService.replaySampleData()
```

A replay button can be added to the toolbar if needed.
