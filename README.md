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

# Force test execution even if inputs/outputs haven't changed
./gradlew test --rerun-tasks
```

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
| `spouseInfo.firstName` | string | no | Accepted but not validated |
| `spouseInfo.lastName` | string | no | Accepted but not validated |
| `spouseInfo.ssn` | string | conditional | Required (non-blank) if maritalStatus is `"married"` |

**Response** (`200 OK`):

```json
{
  "applicationStatus": "VALID" | "INVALID",
  "ruleResults": [
    {
      "ruleId": "STUDENT_AGE",
      "ruleName": "StudentAgeRule",
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
    { "ruleId": "STUDENT_AGE", "ruleName": "StudentAgeRule", "passed": false, ... },
    { "ruleId": "SSN_FORMAT", "ruleName": "SsnFormatRule", "passed": false, ... },
    ...
  ]
}
```

---

## The 7 Edit Rules

| Rule ID | Class | Condition |
|---|---|---|
| `STUDENT_AGE` | `StudentAgeRule` | Must be ≥ 14 and ≤ 120 years old; DOB cannot be a future date |
| `SSN_FORMAT` | `SsnFormatRule` | Must contain exactly 9 digits (dashes and spaces are stripped before check) |
| `DEPENDENT_PARENT_INCOME` | `DependentParentIncomeRule` | If `dependencyStatus == "dependent"`, `parentIncome` is required |
| `INCOME_VALIDATION` | `IncomeValidationRule` | `studentIncome` and `parentIncome` (if present) cannot be negative |
| `HOUSEHOLD_LOGIC` | `HouseholdLogicRule` | `numberInCollege` ≤ `numberInHousehold`; both must be non-negative |
| `STATE_CODE` | `StateCodeRule` | Must be a valid 2-letter US state, DC, or territory abbreviation |
| `MARITAL_STATUS` | `MaritalStatusRule` | If `maritalStatus == "married"`, spouse SSN is required |
