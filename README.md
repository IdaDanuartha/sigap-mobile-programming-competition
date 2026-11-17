# Sigap — Passive Scam-Message Protection for Android

Built for **INDONERIS 2026** (Mobile Programming Category) — *"Realizing Impactful Innovation through Creative and Sustainable Technology"*.

## Overview

**Sigap** ("to guard" in Indonesian) is a passive, privacy-first Android application that reads incoming message notifications from unknown senders across WhatsApp, Telegram, and SMS, warns users before they tap risky links or `.apk` files, explains why in plain language, and provides an emergency recovery workflow with an IASC (Indonesia Anti-Scam Centre) report-draft generator.

## Features Implemented (F1–F10)

- **F1–F4 Passive Notification Scanning & Layered Rule Engine**:
  - `ScamNotificationListenerService` monitors WhatsApp, WhatsApp Business, Telegram, and SMS notifications from unknown senders.
  - Weighted rule engine (`RuleEngine`) detects `.apk`/`.xapk` disguises (wedding invitations, courier package photos, ETLE traffic tickets), URL shorteners, IP hosts, risky TLDs, lookalike bank/e-wallet domains (Levenshtein distance), urgency cues, and OTP/PIN requests.
  - Hard-danger floors for `.apk` attachments and credential requests combined with urgency.
- **F5 "I Clicked It" Emergency Mode & IASC Report Draft Generator (`S07a–S07c`)**:
  - Multi-select situation picker, prioritized interactive action checklist with deep links to App Management and Accessibility Settings, and a structured chronology generator for `iasc.ojk.go.id`.
- **F6 Manual Check Hub & System Share Sheet Target (`S06`)**:
  - On-device OCR (`ML Kit Text Recognition`), barcode/QR decoding (`ML Kit Barcode Scanning`), live QR scanning (`Google Code Scanner`), and `ACTION_SEND` share target for text and screenshots.
- **F7 Family Mode (`S08a–S08f`)**:
  - Consent-first 6-digit invite generator, guardian connection flow, alert dashboard, and alert handling tips without exposing message bodies.
- **F8 Security Checkup (`S09`)**:
  - Weighted 0–100 security score evaluating notification listener access, device screen lock, security patch age, enabled accessibility services, per-app unknown-source installation settings, and Google Play Protect.
- **F9 Community Lookup & Reporting (`S06b`, `S12`)**:
  - E.164 phone normalization (`+62`), bank account digit normalization, registrable domain extraction, SHA-256 hashing, masked display values, and neutral reporting counts.
- **F10 Scam Trends Feed (`S10a`, `S10b`)**:
  - Verified OJK/IASC statistics and bilingual (Indonesian & English) scam-trend articles.

## Architecture & Tech Stack

- **UI**: Jetpack Compose, Material 3 with fixed brand palette (deep green `#14594A` on warm sand `#F6F3EC`), bundled `Manrope` and `Inter` variable fonts, and `Lucide` icons.
- **State & Navigation**: MVVM (`ViewModel` + `StateFlow`), type-safe Navigation Compose with `kotlinx.serialization`.
- **Local Storage**: Room (`sigap.db` — stores redacted snippets only, never full messages) and Jetpack DataStore Preferences, with daily expiration cleanup via `WorkManager` (`PurgeWorker`).
- **Branding Assets**: Original source files are stored in `assets/branding/` (`app-icon.jpeg`, `logo-splash.png`), with generated adaptive launcher icons and splash drawables in `app/src/main/res/`.

## Build & Run

```powershell
# Run unit tests (RuleEngine, Redactor, EntityNormalizer, Emergency, CheckupScorer)
.\gradlew.bat :app:testDebugUnitTest

# Build debug APK
.\gradlew.bat :app:assembleDebug
```
