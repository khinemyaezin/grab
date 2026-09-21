# Architectural Rules (Grab E-Commerce)

You MUST follow every rule in `.agent/rules/` when generating, reviewing, or refactoring code. These rules override any conflicting general coding conventions.

Load `.agent/rules/README.md` first. Then load only the category that matches the files you touch.

Do not invent diagrams. Do not add pictures. Use the MUST / MUST NOT lists as the source of truth.

## Categories

| Category | Path |
|---|---|
| Architecture | `.agent/rules/architecture/` |
| Inbound | `.agent/rules/inbound/` |
| Outbound | `.agent/rules/outbound/` |
| Domain | `.agent/rules/domain/` |
| Platform | `.agent/rules/platform/` |
| Conventions | `.agent/rules/conventions/` |

Before producing code, run the checklist in `.agent/rules/README.md`.
