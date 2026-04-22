# Patient Observation Tracker

## Overview

Patient Observation Tracker is a lightweight clinical record system built using Spring Boot. It allows clinical staff to record patient observations, manage knowledge catalogues, and evaluate diagnostic rules.

This project follows a layered architecture and applies key design patterns such as Strategy, Observer, Factory, and Command.

---

## Tech Stack

* Java 17
* Spring Boot 3.x
* Spring Data JPA
* SQLite
* Maven (with wrapper `./mvnw`)
* HTML, CSS, JavaScript (Vanilla)

---

## How to Run

### 1. Clone the repository

```bash
git clone <your-repo-url>
cd PatientObservationTracker
```

### 2. Ensure database folder exists

```bash
mkdir -p data
```

### 3. Run the application

```bash
./mvnw spring-boot:run
```

### 4. Open in browser

```
http://localhost:8080
```

---

## Features (Week 1)

### F1. Patient Management

* Create and view patients
* Fields: name, date of birth, note

### F2. Phenomenon Type Catalogue

* Create quantitative and qualitative types
* Quantitative: requires allowed units
* Qualitative: requires at least one phenomenon

### F3. Record Measurement

* Record numeric observations with units

### F4. Record Category Observation

* Select a phenomenon and mark it as PRESENT or ABSENT

### F5. Protocol Catalogue

* Create protocols with accuracy ratings

### F6. Diagnostic Rule Evaluation

* Evaluate rules based on patient observations

### F7. Observation List

* View all observations in reverse chronological order

### F8. Reject Observation

* Mark observations as REJECTED with a reason

---

## Important Concepts

### Quantitative vs Qualitative

| Type         | Example       | Input                 |
| ------------ | ------------- | --------------------- |
| Quantitative | Blood Glucose | value + unit          |
| Qualitative  | Thirst        | phenomenon + presence |

### Phenomenon vs Presence

* Phenomenon = WHAT (e.g., Thirst)
* Presence = WHETHER (Present / Absent)

---

## Testing the Application (UI Smoke Test)

1. Create a patient
2. Create phenomenon types:

   * Blood Glucose (QUANTITATIVE, mg/dL)
   * Thirst (QUALITATIVE, phenomenon: Thirst)
3. Create a protocol
4. Record a measurement
5. Record a category observation
6. Reject one observation
7. Create a diagnostic rule
8. Evaluate rules
9. Check logs page
10. Refresh app and verify persistence

---

## Reset Database

To clear all data:

```bash
rm data/tracker.db
```

Restart the app after deletion.

---

## API Endpoints

* GET /api/patients
* POST /api/patients
* GET /api/patients/{id}/observations
* POST /api/observations/measurement
* POST /api/observations/category
* POST /api/observations/{id}/reject
* POST /api/patients/{id}/evaluate
* GET /api/phenomenon-types
* POST /api/phenomenon-types
* GET /api/protocols
* POST /api/protocols
* GET /api/command-log
* GET /api/audit-log

---

## Design Patterns Used

### Strategy

* Used in DiagnosisEngine for rule evaluation

### Observer

* Event listeners react to observation changes

### Factory

* ObservationFactory creates valid observations

### Command

* All actions logged as commands

---

## Notes

* Qualitative types must have at least one phenomenon to appear in dropdowns
* Presence is handled separately (Present/Absent)
* SQLite file is stored in `/data/tracker.db`

---

## Author

Pranav Pattanashetty
# GumballMachine
