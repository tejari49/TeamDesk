# TeamDesk

TeamDesk is a lightweight internal dashboard PWA for daily team operations. It helps colleagues quickly check team availability, handovers, announcements, and internal links.

## Features
- Firebase Authentication (Google popup + dev anonymous fallback)
- Team status updates for today (office/remote/vacation/sick/unavailable)
- Handover creation and completion tracking with filtering
- Today dashboard with greeting, overview, announcements, and quick actions
- Team directory with role and latest status
- Gruppenmanagement (erstellen, Mitglieder sehen, als Gruppenadmin Mitglieder verwalten)
- Profilbereich (Name ändern + Avatar via DiceBear API)
- Nutzer erhalten einen individuellen Code (`userCode`) zum Hinzufügen in Gruppen
- Nutzer sehen im Teambereich nur Mitglieder ihrer eigenen Gruppen
- Gruppenchat + Direktnachrichten in Gruppen mit Bearbeiten/Löschen innerhalb von 1 Stunde
- Quick links grouped by category
- Admin section for announcements and role visibility
- Progressive Web App installability and offline app-shell caching
- HashRouter + Vite base setup for GitHub Pages compatibility

## Stack
- Vite + React + TypeScript
- Firebase JS SDK (Auth + Firestore)
- React Router (`HashRouter`)
- `vite-plugin-pwa`

## Local development
```bash
npm install
npm run dev
```

### Build and checks
```bash
npm run typecheck
npm run build
npm run lint
```

## Firebase setup notes
This repository is preconfigured with the provided Firebase project config in `src/firebase/config.ts`.

Admin users are determined by:
1. email allowlist in `ADMIN_EMAIL_ALLOWLIST` (inkl. `irajet.ramadani@gmail.com`), and/or
2. Firestore user doc `role: "admin"`.

On first Google sign-in, TeamDesk creates/updates `users/{uid}`.

## Firestore deployment
Requires Firebase CLI and project access.

```bash
firebase login
firebase use raikalendercom
firebase deploy --only firestore:rules
firebase deploy --only firestore:indexes
```

## GitHub Pages deployment
A workflow is included at `.github/workflows/deploy.yml`.

1. In GitHub repository settings, enable **Pages** and set source to **GitHub Actions**.
2. Push to `main` (or trigger workflow manually) to publish.
3. The app uses Vite `base` in CI and `HashRouter`, so deep links remain compatible on Pages.

## Seed strategy / first run
No fake production data is auto-seeded.

Recommended initial setup:
- Admin creates at least 1 announcement.
- Admin adds links in two categories (e.g., Ops, HR).
- Team members set status each morning.

## Recommended next steps
- Add richer handover assignment UX and edit flows
- Add role-management action for admins (with additional safeguards)
- Add App Check, analytics, and audit logging
- Add end-to-end tests for core paths
