# AGENTS.md Design

## Goal

Create a root-level `AGENTS.md` for this repository that is optimized for Codex-first collaboration.

The file should not restate the full PRD. It should act as a practical execution guide for future coding sessions in this repo.

## Scope

The document should cover:

- repository purpose and directory split
- requirement sources and how to use `README.md`, `PLAN.md`, `TODO.md`
- local startup commands
- frontend/backend development constraints
- current API/doc access points such as Swagger
- verification expectations
- known pitfalls already observed in this repo

The document should not cover:

- generic AI prompt advice unrelated to this repository
- deep architectural theory already captured in `README.md`
- feature-level implementation details that belong in code or module docs

## Audience

Primary audience:

- Codex operating inside this repository

Secondary audience:

- developers using the repo as a quick operational handbook

## Recommended Structure

Use a layered structure:

1. `Purpose`
2. `Requirement Sources`
3. `Repository Map`
4. `Local Runbook`
5. `Frontend Rules`
6. `Backend Rules`
7. `API and Data Contract Notes`
8. `Documentation and Checklist Rules`
9. `Verification`
10. `Known Project Pitfalls`
11. `Editing Guardrails`
12. `Preferred Codex Workflow in This Repo`

## Key Repository Facts to Encode

- Frontend lives in `frontend/`
- Backend lives in `backend/`
- Frontend default API base is `/api`
- Frontend dev server uses port `9080`
- Backend uses Spring Boot 3 and requires JDK 17+
- Swagger is exposed at `/api/swagger-ui.html` and `/api/docs`
- `README.md`, `PLAN.md`, `TODO.md` are requirement/checklist sources
- Completed checklist items should only be checked after implementation and verification
- This repo has already seen false Vite parser errors caused by malformed `.vue` files and stale dev processes

## Writing Constraints

- Keep the final `AGENTS.md` operational, not verbose
- Use repository-specific facts only
- Prefer instructions that can be executed directly
- Avoid stale assumptions about local Java if verification has already shown Java 8 is still common on this machine

## Acceptance Criteria

The generated `AGENTS.md` is acceptable if:

- it is placed next to `README.md`
- it reflects the current repository structure
- it captures current startup, verification, and integration conventions
- it gives Codex enough guardrails to handle future "按 md 文件修改" tasks without re-deriving repo behavior
