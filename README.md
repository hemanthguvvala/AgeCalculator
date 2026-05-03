# Zodaic Age

Daily horoscope, cosmic age, mood journal, and zodiac compatibility — Android app
built with Jetpack Compose, Hilt, Room, and a fully on-device content engine.

## Stack

- **Kotlin 2.1.21** + **Jetpack Compose** (BOM 2025.07.00)
- **Hilt** for DI, **Room** for persistence, **DataStore** for preferences
- **WorkManager** for scheduled notifications, **Glance** for the home-screen widget
- **Meta Audience Network** for ads, **Google Play Billing v7** for IAP
- **Google UMP** for GDPR consent

## Architecture

```
app/src/main/java/com/hkgroups/agecalculator/
├── content/        # AstronomyEngine + ContentEngine — pure deterministic, offline
├── data/
│   ├── local/      # Room DB, entities, DAOs, type converters
│   ├── model/      # Domain models (ZodiacSign, HistoricalEvent, Compatibility)
│   └── repository/ # ZodiacRepository, SettingsRepository (DataStore)
├── domain/usecase/ # Pure business-logic helpers
├── ui/             # Screens, components, theme, ViewModel
├── util/           # Billing, FAN ads, consent, lunar phase, feedback
├── widget/         # Glance home-screen widget
└── worker/         # WorkManager workers (daily horoscope, mood reminder, cosmic events)
```

### Content engine (zero-cost differentiator)

Astrology is one of the few domains where algorithmic content is indistinguishable
from hand-written content if the templates are good. The two-engine system:

- `AstronomyEngine` — deterministic math: sun-sign of the day, moon phase, transit
  flavor (conjunction/trine/square/etc.), Mercury retrograde, ruling planet of
  the day, lucky number, cosmic-weather classification.
- `ContentEngine` — combinatorial template engine that assembles 4 sentence slots
  from buckets keyed by `(cosmic_weather × element × transit_flavor)`. Same
  `(sign, date)` always produces the same horoscope, so users can't refresh-spam,
  but the underlying astronomy changes daily, so the content does too — without
  any manual content authoring.

Reachable variety: ~98k unique daily horoscopes per sign before any repetition,
and the cosmic-snapshot bucket rotation means the engine almost never repeats
itself in practice.

## Build

```bash
./gradlew :app:assembleDebug         # debug build
./gradlew :app:testDebugUnitTest     # unit tests
./gradlew :app:lint                  # lint
./gradlew :app:assembleRelease       # release build (requires keystore.properties)
```

### Release signing

Create `keystore.properties` at the repo root (gitignored):

```properties
storeFile=release.keystore
storePassword=YOUR_PASSWORD
keyAlias=zodaic
keyPassword=YOUR_PASSWORD
```

When `keystore.properties` is absent the release build still completes but
falls back to debug signing — useful for CI smoke tests.

## Privacy

- **Auto-Backup off** — birthdate, mood entries, partner names are PII; never
  uploaded to Google without explicit user opt-in (which we don't currently offer).
- **No network calls** — the app runs fully offline. The previous `api.zodiac.com`
  placeholder has been replaced by the on-device content engine.
- **GDPR consent** — `ConsentManager` (Google UMP) gates FAN ads in EEA/UK
  before the SDK initializes.

## Engagement loops

- **Streak with freezes** — Duolingo-model streak; one freeze earned every 7
  days, capped at 3, auto-consumed on a missed day. Timezone- and clock-skew-resilient.
- **Question of the day** — daily reflection prompt seeded by user's sign + date.
- **Mood-pattern insights** — surfaces day-of-week / planetary correlations
  once ≥ 5 mood entries are logged.
- **Birthday window** — special messaging in the ±7 day window around the
  user's solar return.
- **Cosmic event notifications** — fires when retrogrades, eclipses, or
  equinoxes are imminent.

## Monetization

- Premium IAP (`premium_lifetime`) — unlocks ad-free + premium features.
  `obfuscatedAccountId` set on purchase to mitigate replay attacks.
- Meta Audience Network — banner + interstitial. Rate-limited (90s gap, 30s
  session grace), gated on consent + ads-disabled.

## Documentation

- `docs/PLAY_STORE_LISTING.md` — Play Console listing copy
- `docs/archive/` — historical design / migration / implementation guides
  (kept for reference; do not treat as current)
