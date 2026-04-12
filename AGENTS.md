# TeamDesk Agent Guidelines

## Project purpose
TeamDesk is a mobile-first internal team dashboard PWA for daily status visibility, handovers, announcements, and quick resource links.

## Stack
- Vite + React + TypeScript
- Firebase Auth + Firestore
- React Router (`HashRouter`) for GitHub Pages
- `vite-plugin-pwa` for installability/offline shell caching

## Architecture notes
- `src/firebase/` contains Firebase initialization and data access layer.
- `src/contexts/AuthContext.tsx` manages session/profile and sign-in flows.
- `src/pages/` contains route-level feature pages.
- `src/components/` contains shared UI wrappers and route guards.

## Auth and roles
- Google sign-in is primary auth.
- Anonymous sign-in is development fallback.
- `users/{uid}.role` determines in-app authorization behavior.
- `ADMIN_EMAIL_ALLOWLIST` in `src/firebase/config.ts` can auto-promote specific emails.

## Firebase wiring
- Config is in `src/firebase/config.ts`.
- Shared Auth/Firestore instances in `src/firebase/client.ts`.
- Firestore helpers in `src/firebase/api.ts`.

## Review guardrails
- Do not weaken `firestore.rules` authentication/authorization constraints.
- Keep GitHub Pages compatibility (`HashRouter` + Vite base strategy) intact.
- Preserve PWA installability and service worker registration.
- Do not add server-side/runtime backend dependencies.
- Maintain mobile-first UX (bottom nav + responsive cards).
