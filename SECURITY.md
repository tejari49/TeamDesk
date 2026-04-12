# TeamDesk Security Notes

## Current MVP security model
- Client-side role checks only drive UI visibility and convenience, not authorization.
- Firestore Security Rules enforce authenticated reads and role-based writes.
- Admin-only writes are enforced for announcements and links.
- Status updates are restricted to the matching authenticated user (`uid`) unless admin.
- Handover updates are restricted to creator/admin.

## Before production rollout
1. Validate roles assignment workflow (allowlist + Firestore role) with your identity policy.
2. Review and tighten handover update permissions if your process requires stricter ownership.
3. Add monitoring and alerts for auth/rules denials.
4. Add Firebase App Check to reduce abuse from untrusted clients.
5. Configure Authentication domain and OAuth consent details for Google sign-in.
