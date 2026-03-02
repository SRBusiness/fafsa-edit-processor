# FAFSA Edit Processor

A Spring Boot service that validates FAFSA (Free Application for Federal Student Aid) application data against a set of business rules ("edits"). All rules are evaluated on every submission, giving applicants a complete picture of every issue at once.

---

## Prerequisites

- Java 17+
- Gradle (wrapper included — no separate install needed)

---

## Build

```bash
./gradlew build
```

## Run Tests

```bash
# All tests
./gradlew test

# Single test class
./gradlew test --tests "com.gditTakeHome.fafsaEditProcessor.rules.StudentAgeRuleTest"
```

# Force test execution even if intputs/outputs haven't changed
./gradlew test --rerun-tasks

## Run the Application

```bash
./gradlew bootRun
```

The service starts on **port 8080**.

---

## API Reference

### POST `/api/applications/validate`

Validates a FAFSA application against all 7 edit rules.

**Request body** (`application/json`):

| Field | Type | Required | Description |
|---|---|---|---|
| `studentInfo.firstName` | string | no | Student first name |
| `studentInfo.lastName` | string | no | Student last name |
| `studentInfo.ssn` | string | yes | Must be exactly 9 digits |
| `studentInfo.dateOfBirth` | date (ISO 8601) | yes | Must be ≥ 14 years ago |
| `dependencyStatus` | string | yes | `"dependent"` or `"independent"` |
| `maritalStatus` | string | yes | `"single"` or `"married"` |
| `household.numberInHousehold` | integer | yes | Total household size |
| `household.numberInCollege` | integer | yes | Must be ≤ numberInHousehold |
| `income.studentIncome` | decimal | yes | Cannot be negative |
| `income.parentIncome` | decimal | conditional | Required if dependencyStatus is `"dependent"` |
| `stateOfResidence` | string | yes | Valid 2-letter US state abbreviation |
| `spouseInfo.firstName` | string | conditional | Required if maritalStatus is `"married"` |
| `spouseInfo.lastName` | string | conditional | Required if maritalStatus is `"married"` |
| `spouseInfo.ssn` | string | conditional | Required if maritalStatus is `"married"` |

**Response** (`200 OK`):

```json
{
  "applicationStatus": "VALID" | "INVALID",
  "ruleResults": [
    {
      "ruleId": "EDIT-001",
      "ruleName": "Student Age",
      "passed": true,
      "severity": "ERROR",
      "message": "Student age is valid (age: 22)."
    }
  ]
}
```

HTTP status is always `200 OK`. Business validity is communicated in `applicationStatus`.

---

## Example Requests

### Valid application

```bash
curl -s -X POST http://localhost:8080/api/applications/validate \
  -H "Content-Type: application/json" \
  -d '{
    "studentInfo": {
      "firstName": "Jane",
      "lastName": "Smith",
      "ssn": "123456789",
      "dateOfBirth": "2003-05-15"
    },
    "dependencyStatus": "dependent",
    "maritalStatus": "single",
    "household": {
      "numberInHousehold": 4,
      "numberInCollege": 1
    },
    "income": {
      "studentIncome": 5000,
      "parentIncome": 65000
    },
    "stateOfResidence": "CA"
  }'
```

Expected response (abbreviated):
```json
{
  "applicationStatus": "VALID",
  "ruleResults": [...]
}
```

### Invalid application (multiple failures)

```bash
curl -s -X POST http://localhost:8080/api/applications/validate \
  -H "Content-Type: application/json" \
  -d '{
    "studentInfo": {
      "firstName": "John",
      "lastName": "Doe",
      "ssn": "123-45-678",
      "dateOfBirth": "2015-01-01"
    },
    "dependencyStatus": "dependent",
    "maritalStatus": "married",
    "household": {
      "numberInHousehold": 2,
      "numberInCollege": 5
    },
    "income": {
      "studentIncome": -1000
    },
    "stateOfResidence": "XX"
  }'
```

Expected response (abbreviated):
```json
{
  "applicationStatus": "INVALID",
  "ruleResults": [
    { "ruleId": "EDIT-001", "ruleName": "Student Age", "passed": false, ... },
    { "ruleId": "EDIT-002", "ruleName": "SSN Format", "passed": false, ... },
    ...
  ]
}
```

---

## The 7 Edit Rules

| Rule ID | Name | Condition |
|---|---|---|
| EDIT-001 | Student Age | Must be ≥ 14 years old |
| EDIT-002 | SSN Format | Must be exactly 9 digits |
| EDIT-003 | Dependent Parent Income | If `dependencyStatus == "dependent"`, `parentIncome` is required |
| EDIT-004 | Income Validation | `studentIncome` and `parentIncome` (if present) cannot be negative |
| EDIT-005 | Household Logic | `numberInCollege` ≤ `numberInHousehold` |
| EDIT-006 | State Code | Must be a valid US state abbreviation (including DC) |
| EDIT-007 | Marital Status | If `maritalStatus == "married"`, spouse info (name + SSN) is required |
