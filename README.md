
# customs-financials-documents-frontend

## About
A frontend microservice for the CDS Financials project, designed to display C79, PVAT, and Notification of Adjustments statements.

| Key                             | Value                                                                                                                     | 
|---------------------------------|---------------------------------------------------------------------------------------------------------------------------|
| Digital service                 | CDS Financials                                                                                                            |
| Local port                      | `9398`                                                                                                                    |
| Base path                       | `/customs/payment-records`                                                                                                |
| Back-end                        | [customs-financials-api](https://github.com/hmrc/customs-financials-api) (port `9878`)                                    |
| Acceptance tests                | [customs-financials-documents-acceptance-tests](https://github.com/hmrc/customs-financials-documents-acceptance-tests)    |
| Performance tests               | [customs-financials-performance-tests](https://github.com/hmrc/customs-financials-performance-tests)                      |

## How to Run this Service

### Prerequisites
Use [Service Manager](#service-manager-profiles) to start the services required by CDS Financials.

From the terminal, run:

```bash
sm2 --start CUSTOMS_FINANCIALS_ALL
```

### Running the service locally
To run this service from source (for example, to test your own changes), stop the instance started by Service Manager and run it with sbt.

From the root of this repository, run:

```bash
sm2 --stop CUSTOMS_FINANCIALS_DOCUMENTS_FRONTEND
sbt run
```
The service starts on port `9398`.

## How to Test this Service

### Local

#### Unit tests
```bash
sbt test
```

#### Pre-push check
This is a project-specific sbt command alias that runs Scala style checks, executes unit and integration tests, and generates a code coverage report:
```bash
sbt runAllChecks
```

#### Acceptance tests (smoke and regression)
The acceptance tests for this service are in [customs-financials-documents-acceptance-tests](https://github.com/hmrc/customs-financials-documents-acceptance-tests). For information about how to run them, and which environments they run against, see [README](https://github.com/hmrc/movements-internal-ui-acceptance-tests#how-to-run-the-tests).

#### Performance tests
The performance tests for this service are in [customs-financials-performance-tests](https://github.com/hmrc/customs-financials-performance-tests). For information about how to run them, and which environments they run against, see [README](https://github.com/hmrc/cds-movements-performance-tests#how-to-run-the-tests).

### Manual Testing in QA and Staging
After a successful [deployment pipeline](https://build.tax.service.gov.uk/job/BordersAndTradeLiveServices/job/CDSFinancials/job/customs-financials-documents-frontend-pipeline/), perform manual verification in QA and/or Staging using the [manual testing instructions](https://github.com/hmrc/customs-financials-documents-acceptance-tests#manual-testing-in-qa-and-staging).

## Service Catalogue
- [customs-financials-documents-frontend in the MDTP Catalogue](https://catalogue.tax.service.gov.uk/repositories/customs-financials-documents-frontend)

## Jenkins Pipeline
Front End Build and Deployment Pipeline
-  [Build pipeline](https://build.tax.service.gov.uk/job/BordersAndTradeLiveServices/job/CDSFinancials/job/customs-financials-documents-frontend/)
-  [Deployment pipeline](https://build.tax.service.gov.uk/job/BordersAndTradeLiveServices/job/CDSFinancials/job/customs-financials-documents-frontend-pipeline/)

The acceptance test Jenkins jobs (smoke and regression) are listed in the [movements-internal-ui-acceptance-tests README](https://github.com/hmrc/movements-internal-ui-acceptance-tests#jenkins-builds).

The performance test Jenkins jobs are listed in the [cds-movements-performance-tests README](https://github.com/hmrc/cds-movements-performance-tests#jenkins-builds).

## Service Manager Profiles
These profiles are defined in [service-manager-config](https://github.com/hmrc/service-manager-config).

| Profile                     | Use                                                                                                                        | Services started                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                  |
|-----------------------------|----------------------------------------------------------------------------------------------------------------------------|-----------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------|
| `CUSTOMS_FINANCIALS_ALL`    | Running and developing this service locally                                                                                | `CUSTOMS_FINANCIALS_FRONTEND`, `CUSTOMS_FINANCIALS_DOCUMENTS_FRONTEND`, `CUSTOMS_FINANCIALS_SECURE_MESSAGING_FRONTEND`, `ENTITY_RESOLVER`, `CHANNEL_PREFERENCES`, `SECURE_MESSAGE_FRONTEND`, `SECURE_MESSAGE`, `SECURE_MESSAGE_STUB`, `MESSAGE`, `CUSTOMS_DUTY_DEFERMENT_FRONTEND`, `CUSTOMS_MANAGE_AUTHORITIES_FRONTEND`, `CUSTOMS_CASH_ACCOUNT_FRONTEND`, `CUSTOMS_GUARANTEE_ACCOUNT_FRONTEND`, `CUSTOMS_HISTORIC_STATEMENT_FRONTEND`, `CUSTOMS_FINANCIALS_API`, `CUSTOMS_FINANCIALS_EMAIL_THROTTLER`, `AUTH`, `AUTH_LOGIN_STUB`, `AUTH_LOGIN_API`, `CENTRALISED_AUTHORISATION_SERVER`, `SSO`, `USER_DETAILS`, `FEEDBACK_FRONTEND`, `CUSTOMS_FINANCIALS_HODS_STUB`, `CUSTOMS_FINANCIALS_SDES_STUB`, `CUSTOMS_FINANCIALS_SDDS_STUB`, `IDENTITY_VERIFICATION`, `BAS_STUBS`, `BAS_GATEWAY`, `BAS_PROXY`, `BAS_GATEWAY_FRONTEND`, `TWO_STEP_VERIFICATION_RULE_ENGINE`, `TAR_FRONTEND`, `DATASTREAM`, `EMAIL`, `HMRC_EMAIL_RENDERER`, `CUSTOMS_DATA_STORE`, `CUSTOMS_FINANCIALS_SESSION_CACHE`, `TRACKING_CONSENT_FRONTEND`, `CONTACT_FRONTEND`, `ACCESSIBILITY_STATEMENT_FRONTEND`  |
| `CUSTOMS_FINANCIALS_ATS`    | Running the [acceptance tests](https://github.com/hmrc/customs-financials-documents-acceptance-tests#how-to-run-the-tests) | `CUSTOMS_FINANCIALS_FRONTEND`, `CUSTOMS_FINANCIALS_DOCUMENTS_FRONTEND`, `CUSTOMS_FINANCIALS_SECURE_MESSAGING_FRONTEND`, `SECURE_MESSAGE_FRONTEND`, `SECURE_MESSAGE`, `CUSTOMS_DUTY_DEFERMENT_FRONTEND`, `CUSTOMS_MANAGE_AUTHORITIES_FRONTEND`, `CUSTOMS_CASH_ACCOUNT_FRONTEND`, `CUSTOMS_GUARANTEE_ACCOUNT_FRONTEND`, `CUSTOMS_HISTORIC_STATEMENT_FRONTEND`, `CUSTOMS_FINANCIALS_API`, `CUSTOMS_FINANCIALS_EMAIL_THROTTLER`, `AUTH`, `AUTH_LOGIN_STUB`, `AUTH_LOGIN_API`, `USER_DETAILS`, `CUSTOMS_FINANCIALS_HODS_STUB`, `CUSTOMS_FINANCIALS_SDES_STUB`, `IDENTITY_VERIFICATION`, `BAS_STUBS`, `BAS_GATEWAY`, `BAS_PROXY`, `BAS_GATEWAY_FRONTEND`, `BAS_STUB_FRONTEND`, `DATASTREAM`, `EMAIL`, `FEEDBACK_FRONTEND`, `HMRC_EMAIL_RENDERER`, `CUSTOMS_DATA_STORE`, `CUSTOMS_FINANCIALS_SESSION_CACHE`, `TRACKING_CONSENT_FRONTEND`                                                                                                                                                                                                                                                 |

## Endpoints

| Method   | Path                    | Purpose                                                                | Sample request (query / form body)                                                      | Response                                                                                                                     |
|----------|-------------------------|------------------------------------------------------------------------|-----------------------------------------------------------------------------------------|------------------------------------------------------------------------------------------------------------------------------|
| GET      | `/postponed-vat`        | Retrieve postponed vat / historic postponed vat statements             | Optional query parameter: location. Example: `/postponed-vat?location=historic`         | `200` renders postponed VAT statements page. `303` → `/postponed-vat/statements-unavailable` if retrieval fails.             |
| GET      | `/adjustments`          | Retrieve security statements for EORI                                  | –                                                                                       | `200` renders security statements page. `303` → `/adjustments/statements-unavailable` if retrieval fails.                    |
| GET      | `/import-vat `          | Retrieve vat certificates for EORI                                     | –                                                                                       | `200` renders VAT certificates page. `303` → `/import-vat/certificates-unavailable` if retrieval fails.                      |

## Enrolment Required

To access the Customs Financials Frontend, use the following enrolment details and sample EORI numbers.

| Environment      | Service URL                                                         |
|------------------|---------------------------------------------------------------------|
| Local            | http://localhost:9949/auth-login-stub/gg-sign-in                    |
| QA               | https://www.qa.tax.service.gov.uk/auth-login-stub/gg-sign-in        |
| Staging          | https://www.staging.tax.service.gov.uk/auth-login-stub/gg-sign-in   |

Redirect URL: `/customs/payment-records`

Use the following enrolment details when signing in:

| Enrolment Key  | Identifier Name | Identifier Value |
|----------------|-----------------|------------------|
| `HMRC-CUS-ORG` | `EORINumber`    | `GB744638982000` |
| `HMRC-CUS-ORG` | `EORINumber`    | `GB744638982001` |

### Feature flags
The following feature flags are used across the service:

| Flag                          | Description                                                                                                                                         |
|-------------------------------|-----------------------------------------------------------------------------------------------------------------------------------------------------|
| `fixed-systemdate-for-tests`  | Enable the fixed datetime for DateTimeService by enabling the stub data for current and requested statements. It is only enabled in Development env |
| `historic-statements-enabled` | Enable historic statements                                                                                                                          |

### Scalafmt
The code is formatted with [sbt-scalafmt](https://scalameta.org/scalafmt/docs/installation.html#sbt), using the rules in `.scalafmt.conf`.

Check that all project files are formatted as expected:

```bash
sbt scalafmtCheckAll scalafmtSbtCheck
```

Format `*.sbt` and `project/*.scala` files:

```bash
sbt scalafmtSbt
```

Format all project files:

```bash
sbt scalafmtAll
```

## License
This code is open source software licensed under the [Apache 2.0 License]("http://www.apache.org/licenses/LICENSE-2.0.html").