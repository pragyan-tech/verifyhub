# VerifyHub

A document verification backend service for KYC workflows. Handles secure document upload, integrity checks, deduplication, and verification state management with full audit trails.

Built as a focused exploration of the backend infrastructure problems in identity verification systems.

## Tech Stack

- **Language:** Java 21
- **Framework:** Spring Boot 3.5
- **Security:** Spring Security + JWT
- **Database:** MySQL 8
- **ORM:** Hibernate / Spring Data JPA
- **Build:** Maven
- **Containerization:** Docker (planned)

## Core Features (in development)

- JWT-based authentication with role-based access (USER / ADMIN)
- Secure document upload with server-side validation (magic-byte MIME check, size limits)
- SHA-256 content hashing for deduplication (detects re-submitted documents)
- EXIF/metadata stripping on image uploads (privacy + tamper-detection prep)
- Verification workflow state machine: `PENDING → UNDER_REVIEW → VERIFIED / REJECTED`
- Append-only audit log for all document lifecycle events (compliance-ready)
- Admin review endpoints with rejection-reason tracking

## Architecture

Documented as the project develops. High-level: a stateless REST API backed by MySQL, with files persisted to disk (production target: object storage like S3/MinIO).

## Status

🚧 Active development.

## Author

Pragyan Oza — [LinkedIn](https://linkedin.com/in/pragyan-oza) · [GitHub](https://github.com/pragyan-tech)