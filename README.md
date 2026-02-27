# PlanWise (v0.1)

PlanWise ist ein werbefreier, minimalistischer Offline‑Organizer für **Ereignisse** und **Schichten** – ohne Konto, ohne Tracking, ohne verpflichtende Cloud.
Optional kann eine **Supabase**‑Synchronisation aktiviert werden (**Default: AUS**).

## Setup / Run
1. ZIP entpacken
2. In **Android Studio** öffnen (Gradle Sync)
3. `app` starten (minSdk 26)

**Hinweis (Notifications):**
- Ab Android 13 (API 33) benötigt die App die Runtime‑Permission `POST_NOTIFICATIONS`, sonst erscheinen Erinnerungen ggf. nicht.
- Exakte Alarme (API 31+) können eine Nutzerfreigabe benötigen (`SCHEDULE_EXACT_ALARM`). PlanWise versucht eine sinnvolle Fallback‑Strategie.

## Architektur (Clean-ish)
- `domain/` – Modelle, UseCases, Repository‑Interfaces
- `data/` – Room, DataStore, Repository‑Implementierungen, Export/Import
- `presentation/` – Compose UI, ViewModels (MVVM)
- `reminders/` – AlarmManager + Receiver + Boot Reschedule
- `sync/` – Supabase (optional, Feature‑Flag)
- `widget/` – klassisches AppWidget

## Reminder‑Funktionsweise (lokal)
Beim Speichern eines Events werden pro Reminder‑Offset Alarme geplant.  
Ein Reboot/Timezone‑Change triggert den `BootReceiver`, der alle kommenden Alarme neu plant.

## Export/Import
- Export: JSON (Events + Shifts) in App‑Cache → Share Intent
- Import: JSON via Storage Access Framework

## Supabase (optional)
PlanWise nutzt **nur Publishable/Anon Keys** (keine `service_role` Keys).  
Empfohlen: **RLS** aktivieren, damit Nutzer nur eigene Daten sehen.

### Minimal-Schema (Beispiel)
Siehe `docs/supabase_schema.sql`.

### Auth
Wenn Cloud‑Sync aktiv ist, meldet sich die App per `signInAnonymously()` an und speichert die Session lokal.

## Lizenz / Kosten
- Keine bezahlpflichtigen APIs erforderlich
- Keine Firebase Abhängigkeit in der Default‑Konfiguration
- Keine Analytics/Tracking SDKs
