# External RAG Sample: OWASP Top 10 A01 A03 A05

Source URLs:

- https://owasp.org/Top10/2021/A01_2021-Broken_Access_Control/
- https://owasp.org/Top10/2021/A03_2021-Injection/
- https://owasp.org/Top10/en/A05_2021-Security_Misconfiguration/

Prepared for: close-concept classification, security category retrieval, weak-related refusal.
Snapshot note: compact offline sample prepared on 2026-05-23 from OWASP Top 10 2021 pages.

## A01 Broken Access Control

Access control enforces policy so users cannot act outside their intended permissions. Broken access control occurs when that policy is missing, bypassed, or incorrectly enforced.

Typical failures include:

- Violation of least privilege or deny by default.
- URL or parameter tampering to bypass access checks.
- Insecure direct object references, also called IDOR.
- Missing access controls on POST, PUT, or DELETE API operations.
- Privilege escalation from unauthenticated user to user or from user to administrator.

Broken access control is primarily an authorization failure. A good answer should focus on permission boundaries, object ownership, and server-side enforcement.

## A03 Injection

Injection occurs when untrusted input reaches an interpreter or command context without proper handling. An application is vulnerable when user-supplied data is not validated, filtered, or sanitized, or when dynamic queries and non-parameterized calls are used directly in an interpreter.

Typical examples include SQL injection, cross-site scripting, command injection, and unsafe file path control.

Expected retrieval points:

- user-supplied data not validated, filtered, or sanitized.
- dynamic queries.
- non-parameterized calls.

## A05 Security Misconfiguration

Security misconfiguration covers unsafe defaults, missing hardening, excessive services, missing security headers, verbose error messages, outdated components, and insecure framework or server settings.

The application might be vulnerable if:

- Security hardening is missing across the stack.
- Unnecessary ports, services, pages, accounts, or privileges are enabled.
- Default accounts and passwords remain enabled.
- Error handling reveals stack traces or overly informative messages.
- Security headers or directives are missing or unsafe.
- Upgraded systems leave newer security features disabled.

## Category Differences

Broken Access Control, Injection, and Security Misconfiguration are related but not interchangeable.

- Broken Access Control is about authorization policy failure.
- Injection is about unsafe data flowing into interpreters or command contexts.
- Security Misconfiguration is about unsafe system, framework, server, or deployment settings.

The same incident can involve more than one category, but a precise answer should identify the primary failure mode.

## Boundary Notes

This sample does not define OAuth 2.1 authorization code flow message formats. It can discuss access-control risk, but it cannot provide the complete OAuth protocol sequence unless another OAuth specification document is available.
