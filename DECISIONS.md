# Design Decisions

## Process
I took about ~4 hours to do this. A good hour of that was setting up my dev env (IDE, terminal, git, Claude Code) because this was the first time I coded on my new personal machine. I used Claude Code to help me plan and implement this code, followed by iterations of manually reviewing and using Claude to fix
issues. I took a break and then came back the next day to write out the decisions doc. I definitely should have made many more smaller commits during my iteration process, but I got lost in the process and didn't want to go too far over the time limit, so I kept going. 

My ground rules for Claude that live in my global CLAUDE.MD

- Don't flatter me. Be charming and nice, but very honest. Tell me something I need to know even if I don't want to hear it
- I'll help you not make mistakes, and you'll help me
- Make implementation decisions within the scope of what we've discussed,  but check with me on anything architectural or hard to reverse. Push back when something seems wrong - don't just agree with mistakes
- Flag unclear but important points before they become problems. Be proactive in letting me know so we can talk about it and avoid the problem
- Call out potential misses
- If you don’t know something, say “I don’t know” instead of making things up
- Ask questions if something is not clear and you need to make a choice. Don't choose randomly if it's important for what we're doing
- When you show me a potential error or miss, start your response with❗️emoji
- When you're about to do something that can't be easily undone (delete files,  overwrite code, etc.), confirm with me first

## 1.  Tech  Stack

I used Java + spring initializr because I'm most familiar with it and it was an easy out of the box starting point. 

## 2. Data Models

The data shapes used for incoming requests and outgoing responses are defined separately from each other. This keeps the API contract clean and makes it easier to change one without affecting the other.

I used Lombok to cut down on repetitive boilerplate code (getters, setters, and constructors) that would otherwise need to be written out manually for every class. I know Lombok is polarizing but I'm a fan and it allowed me to move faster.

One deliberate choice I made is that parent income is treated as an optional field at the data level, even though it's required in certain situations. Rather than enforcing that requirement at the data parsing stage, it's left to the validation rules to catch  which keeps all the conditional logic in one predictable place instead of spread across multiple layers.

## 3. Rule Representation: Strategy Pattern with Spring DI

Each validation rule is a separate class annotated with `@Component` that implements the `EditRule` interface:

```java
public interface EditRule {
    RuleResult apply(ApplicationRequest application);
    String getRuleId();
    Severity getSeverity();
}
```

Spring automatically discovers all `EditRule` beans and injects them as a `List<EditRule>` into `EditProcessorService`.

Alternatives I considered
- Proper rules engine:
    - Drool: lots of features, heavy weight, not sure it is the right for a FAFSA app, juice wasn't worth the squeeze for a time capped project when I'm not very familiar with it.
    - EasyRules: lots of features but more light weight that Drool. I've never used it and didn't want to start for this take home but seems like a decent fit for the use case.
- Configuration-driven approach:  where rules are defined in YAML or a DB - opted not to go that route given the time constraints. The requirements as written IMO didn't justify the additionally complexity it would add. My current approach could be updated to apply a configuration based approach in the future.

## 4. Extensibility

Adding a rule requires creating one new class annotated with `@Component` that implements `EditRule`. Spring auto-discovers it and the service picks it up automatically.

## 5. Conditional Rules

Two of the seven rules only apply in certain situations:
- `DependentParentIncomeRule`: Parent income is only required if the student is dependent
- `MaritalStatusRule`: Spouse information is only required if the student is married

Rather than skipping these rules entirely when they don't apply, both rules still run and return a passing result with a note explaining why. For example "Applicant is not dependent; parent income not required." This means every application always gets all 7 results back, which makes the response predictable and easier to work with on the front end.

The rules use defined status types rather than raw text comparisons, so an unexpected or missing value defaults to passing rather than accidentally triggering the wrong behavior.

## 6. Rule Priority and Ordering

Currently, the rule execution order has no functional impact. Each rule runs no matter what and doesn't have any logic that depends on another's result, so the outcome is identical regardless of the order rules are applied. Spring makes no ordering guarantee for component-scanned beans collected into a `List<EditRule>`, which means the order of rules applied and the  `ruleResults` in the response is non-deterministic between restarts.

If we wanted to have the rules run in a predictable order we could add `@Order` to each rule class.

If in the future a rule was dependent on the result of another rule we would need to refactor to create a wrapper object to pass around the accumulated results from one rule to another.

## 7. Error Handling: Collect All Errors, Not Fail-Fast

All 7 rules run against every application regardless of earlier failures.  I assumed that in the real world FAFSA processing applicants want to see every problem at once versus discovering them one by one.  Additionally I assume that front end from would have its own validation and features to help users submit applications with valid values.

## 8. Rule Conflicts

Each rule is responsible for checking exactly one thing, and the rules are designed so they don't step on each other.

ex: `DependentParentIncomeRule` checks whether parent income was provided at all, and `IncomeValidationRule` checks whether the value is valid (not negative). `IncomeValidationRule` only runs its check if a value actually exists, so if parent income is missing, you get exactly one error message, not two.

If two rules ever did contradict each other, the system would report both failures and flag the application as invalid. But that situation is treated as a design mistake, not something the code should try to resolve automatically. I took the approach that the authors would write clear well defined rules and that we would use unit/integ test to catch conflicts.

## 9. Severity Levels

There are two severity levels: `ERROR` and `WARNING`. All 7 rules use `ERROR` because every violation they catch is a hard blocker: an invalid SSN, a student under 14, or a negative income aren't edge cases to flag for review, they're problems that prevent the application from moving forward.

`WARNING` was included because it seemed like an obvious future need, even though none of the current requirements called for it. It's the kind of thing you'd want for unusual-but-not-disqualifying situations  (Ex. a parent income of $0 for a dependent student, or a household size outside typical ranges). The infrastructure is already in place and the service already handles warnings correctly, only marking an application invalid when an `ERROR` is present.

A limitation of the current design is that severity is set at the rule level, meaning every outcome from a given rule gets the same severity. To use `WARNING` the code would need to be updated to let each brach within a rule set its own severity.

## 10. Application Status

An application is either `VALID` or `INVALID`. I considered adding a third status, something like "needs review" for cases where only minor issues are flagged, but I didn't see a clear use-case for it given that the 7 current rules to all appeared to be hard blockers. This could be added in the future if the right use-case requirement case up.

## 11. HTTP Status vs. Business Status

The API always returns a `200` response as long as the request was received and processed successfully. Whether the application passed or failed validation is communicated in the response body, not through the HTTP status code.

My reasoning: HTTP status codes are meant to communicate whether the request itself succeeded. Did the server receive it, understand it, and process it? If the answer is yes, the request succeeded. Whether the applicant's FAFSA data is valid is a separate, business-level concern that I thought that belonged in the response body.

The alternative was to return a `4xx` response for a failed validation forcing the client to parse errors from the body which is awkward.

## 12. State Code Validation

Valid state codes are stored as a hardcoded list of 56 codes (50 states, DC, and 5 US territories = PR, VI, GU, AS, MP). This is solution is verbose but simple and fast which felt like the right fit for data that rarely changes in the context of a takehome assignment.  I also thought about adding military postal codes (AA, AE, AP) but didn't make that lift, it is something that can be added in the future if needed.

My assumption is that in production that is small shared library that services take a dependency on and all use.

Alternatives
- Use Apache common lang or Java's built in `Locale`
- Take a dependency on a 3rd party library
- API call to an external service - over kill and I wanted to avoid a dependency and a network call
- Enum - would definitely work but the set of string was simpler and worked

## 13. Parse Error Handling (`@RestControllerAdvice`)

A global error handler `GlobalExceptionHandler` catches requests that can't be parsed before they reach the validation logic, and returns a clean `400` response with a simple error shape aka just an error code and a human-readable message.

Two decisions points worth explaining:

- Why a different response shape than a normal validation result? When the request body can't be parsed, no rules can run. Returning an empty list of rule results with an unclear status would be misleading. A flat error response signals that something went wrong at the request level, before any business logic was involved. It is a different category of problem that deserves a different response shape.
- Why rewrite the error message instead of passing through the raw one? The default error messages from the JSON parsing library includes internal class names and package paths that are noisy and leak implementation details. The error handler inspects the specific type of parse failure and rewrites it into a clear, actionable message. For example, it tells the caller which field was invalid and what format was expected, without exposing anything about how the service is built internally.

One technical note: this project uses Spring Boot 4, which ships with a newer version of the Jackson JSON library that moved some classes to different package paths and renamed some methods compared to older versions. The error handler accounts for these differences.

## 14. Validation Message Strategy

In a production system I would likely take a different approach and do something like storing messages in an external properties file, which would allow them to be updated without a code change and would support multiple languages if needed.  For this assignment, I kept the error messages as names constants at the top of each rule class because it seemed like the fastest cleanest solution.

## 15. Clock Injection in StudentAgeRule

The `StudentAgeRule` uses an injected clock rather than reading the system time directly. `LocalDate.now()` reads the JVM system clock, which is known to cause subtle issues b/c it reflect the timezone of the machine running the code, meaning that the same test could return different dates depending on where it was run (ex. dev lap top vs a production server in a different timezone).

By injecting a clock, tests can freeze time at any specific date, making the results consistent and repeatable regardless of when the tests are run. In production, the real system clock is used, so actual behavior is unchanged.

## 16. Rule ID Format

Rule IDs use descriptive `SCREAMING_SNAKE_CASE` (e.g., `STUDENT_AGE`, `SSN_FORMAT`) rather than opaque sequential codes (e.g., `EDIT-001`). Sequential codes require a reference document to decode; descriptive IDs are self-explanatory in logs, API responses, and client error handling. There is no external spec that mandates the `EDIT-00X` format, so readability wins.

## 17. Test Approach

I organized the tests in 3 layers
1. **Unit Tests:** Individual rule unit tests are in plan java. Each rule is tested in isolation with the minimum data needed to trigger the behavior.
2. **Service integration test:** Tests the full pipeline manually with all 7 rules and runs complete application scenarios end to end. This catches any issues with how rules interact as a group, still without the overhead of loading the full framework.
3. **Controller Test:** loads the full application and exercises the entire HTTP stack: from receiving a JSON request to returning a response. This is where JSON serialization, error handling, and HTTP behavior are verified.

## 18. Performance

I made this a extremely simple MVP which is pretty performant. Each rule is self-contained and only reads the application data with no database calls, no external requests, just looking at fields and comparing values. This makes each individual validation essentially instant. B/c rules don't store any state between requests, the service can be easily scaled horizontally. If the number of rules grew significantly, rules could be run in parallel with a one-line code change. For 7 fast rules it's not worth it, but the design supports it.

## 19. TODO If I Had More Time

Items I thought about but couldn't implement given the time constraints

- **Structured logging**: Log request metadata like timing and outcomes ***without*** logging the request body itself, which contains sensitive PII like SSN and income data. A production implementation would use structured logs with a unique ID per request for traceability.
- **Stable rule ordering**: Spring does not guarantee the order in which it loads rules (beans), so the order of results in the response is currently unpredictable. Adding an explicit order to each rule class would make the response consistent and easier to work with.
- **Per-result severity**: Currently every outcome from a given rule gets the same severity level. A better design would let each branch inside a rule set its own severity, so the same rule could return a hard error for one input and a soft warning for another.
- **Review required status**: A third application status for "flagged for review" was considered but deferred. It makes sense to introduce it alongside the first rule that actually needs it.
- **API versioning**: Prefix the endpoint with `/api/v1/` so future changes to the request or response shape do not break existing callers.
- **Health checks**: Expose a standard health endpoint so load balancers and container orchestration tools can monitor whether the service is up.
- **Externalized  configuration**: move hardcoded thresholds (`MINIMUM_AGE`, valid state codes) and enable/disable flags to an external config store so rule parameters can be updated without a redeploy.
- **Audit trail**: Persist a record of every validation request and its outcome. FAFSA is a regulated domain gov entity and I assume production requirement would include the ability to do an audit history.
- **API Authentication:** Currently no auth layer and it would absolutely be a requirement for production. API Key validation or OAuth tokens to ensure that only authorized users can submit applications and receive results.
- **Input sanitization**: Prod service should sanitize inputs to guard against injection attacks. Current service places a lot of trust in well formatted JSON data is receives.
- **Rate Limiting**: Current service has no protections against a caller hammering the service with requests and, ensure fair usage, cost control, and avoid being brought down by DDOS.
- **Containerization**:  put it in a container (docker) so that is can be consistently used in different environments
