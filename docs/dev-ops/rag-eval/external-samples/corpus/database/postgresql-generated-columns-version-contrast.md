# External RAG Sample: PostgreSQL Generated Columns Version Contrast

Source URLs:

- https://www.postgresql.org/docs/18/ddl-generated-columns.html
- https://www.postgresql.org/docs/16/sql-createtable.html

Prepared for: version-aware retrieval, close-term contrast, conflict handling.
Snapshot note: compact offline sample prepared on 2026-05-23 from PostgreSQL 18 generated column docs and PostgreSQL 16 CREATE TABLE docs.

## PostgreSQL 18 Generated Columns

In PostgreSQL 18, a generated column is a column computed from other columns. It is conceptually similar to how a view is computed from a table.

PostgreSQL 18 describes two kinds of generated columns:

- stored.
- virtual.

A stored generated column is computed when the row is written, during insert or update, and it occupies storage like a normal column. A virtual generated column occupies no storage and is computed when it is read.

In PostgreSQL 18, a generated column is virtual by default unless `VIRTUAL` or `STORED` is specified explicitly.

## PostgreSQL 18 Restrictions

Generated columns have restrictions. Important restrictions include:

- The generation expression can only use immutable functions.
- The generation expression cannot use subqueries.
- The generation expression cannot reference another generated column.
- A generated column cannot have a column default or identity definition.
- A generated column cannot be part of a partition key.

For virtual generated columns, additional restrictions apply to user-defined types and functions. Stored generated columns do not have the same virtual-column restriction.

## PostgreSQL 16 CREATE TABLE Context

PostgreSQL 16 documentation for `CREATE TABLE` uses generated-column syntax with `GENERATED ALWAYS AS (...) STORED`. In this PostgreSQL 16 context, generated columns are represented as stored generated columns. A PostgreSQL 16 answer should not claim the PostgreSQL 18 default virtual behavior unless the question explicitly asks for PostgreSQL 18 or includes PostgreSQL 18 documentation.

## Version Conflict Handling

If a knowledge base contains both PostgreSQL 16 and PostgreSQL 18 documentation, the answer must distinguish versions.

Correct version-aware answer:

- PostgreSQL 18 has stored and virtual generated columns.
- PostgreSQL 18 defaults to virtual when the kind is not specified.
- PostgreSQL 16 `CREATE TABLE` documentation shows generated columns as `STORED`.
- Do not apply the PostgreSQL 18 default virtual conclusion to PostgreSQL 16 without version qualification.

## Boundary Notes

This sample covers generated columns. It does not explain MySQL invisible columns, Oracle virtual columns, or database index tuning unless another document is present.
