# External RAG Sample: RFC 9110 HTTP Semantics

Source URL: https://www.rfc-editor.org/rfc/rfc9110
Prepared for: standards semantics, exact term retrieval, framework-boundary refusal.
Snapshot note: compact offline sample prepared on 2026-05-23 from RFC 9110 HTTP semantics.

## Scope

RFC 9110 defines HTTP semantics: request methods, request and response metadata, status codes, representation metadata, content negotiation, and related concepts. It separates the meaning of HTTP messages from the exact wire syntax used by a particular HTTP protocol version.

HTTP core semantics do not change just because HTTP/1.1, HTTP/2, or HTTP/3 use different message framing. The expression on the wire can change between protocol versions, while the semantic meaning can remain stable. HTTP also allows incremental, backward-compatible extensions through defined extension points.

## Status Codes

A status code is a three-digit integer code. It describes the result of the request and the semantics of the response. Status codes are grouped by their first digit into classes such as informational, successful, redirection, client error, and server error.

Expected exact retrieval points:

- three-digit integer code.
- result of the request.
- semantics of the response.

## Informational 1xx

Informational status codes use the 1xx class. HTTP/1.0 did not define 1xx status codes. A server MUST NOT send a 1xx response to an HTTP/1.0 client.

This section is useful for literal matching because the expected answer must include both the HTTP/1.0 boundary and the prohibition on sending 1xx responses to that client type.

## Core Semantics And Wire Format

HTTP semantics include what a method means, what status code classes indicate, and what header fields or content metadata communicate. Wire format is how those semantics are represented in a protocol version. For example, HTTP/2 and HTTP/3 can change framing while still preserving method and status code semantics.

An answer about this distinction should not drift into a framework-specific implementation unless another document provides that implementation.

## Framework Boundary

RFC 9110 is not a Spring Boot guide. It does not specify how to implement `@ControllerAdvice`, how to map Java exceptions, or how to structure a JSON error body in a Spring application. It can support general HTTP status-code reasoning, but not framework-specific code generation.
