# Contributing

## Frontend Rules

- Use external template files for Angular components: prefer `templateUrl` over inline `template`.
- Use external stylesheet files for Angular components: prefer `styleUrl` or `styleUrls` over inline `styles`.
- Keep component files grouped together in the same folder:
  - `feature.component.ts`
  - `feature.component.html`
  - `feature.component.css`
- Put component logic in the TypeScript file only; keep markup in HTML and presentation styles in CSS.
- Follow the existing Nx app structure under `frontend/apps/pos-app/src/app`.

## Backend Rules

- Keep controllers thin.
- Put business logic in services/use cases.
- Prefer the existing hexagonal structure under `adapters`, `application`, and `core/port` for new backend features.

## General Rules

- Do not create a new project when updating an existing feature.
- Keep task work isolated on a dedicated feature branch.
- Verify changes locally before pushing when the task affects runtime behavior.
- When opening a PR for an issue-backed task, include an auto-close keyword in the PR body, for example `Closes #9`.
