# Release Readiness Checklist — Zodaic Age v1.1

This is the honest list of what still needs to happen before pushing to the
Play Store. Items marked ✅ are verified by automated checks; items marked
🟡 require manual / on-device verification that hasn't been done.

## Code-level (verified)

- ✅ Debug + release builds compile
- ✅ R8 minification + resource shrinking enabled on release
- ✅ Release APK ~7 MB (down from 22 MB debug)
- ✅ 34 unit tests pass: streak edge cases, content engine determinism,
  astronomy math, zodiac sign use case, midnight ticker
- ✅ Lint passes (`./gradlew :app:lintDebug`)
- ✅ No references to removed symbols (BASE_URL, USE_MOCK_API,
  ZodiacApiService, MockApiInterceptor, getFreshZodiacSign)
- ✅ AndroidManifest reviewed: `allowBackup="false"`,
  `dataExtractionRules` set, `RECEIVE_BOOT_COMPLETED` removed,
  orientation lock removed
- ✅ Room: `exportSchema = true`, `fallbackToDestructiveMigrationFrom(1)`
  safety net in place
- ✅ `local.properties` and `.idea/` untracked from git
- ✅ Stray `.kt` guide files at repo root removed from git
- ✅ Release `proguard-rules.pro` strips `Log.d/v/i` via `assumenosideeffects`

## Manual / on-device verification (NOT done)

### 🟡 First-launch flow

Need to install the release APK on a fresh device or emulator and verify:

- [ ] App launches without crash
- [ ] Splash screen shows for ~1 second then dismisses
- [ ] Welcome screen appears (no birth date saved)
- [ ] Date picker opens, user can pick a date
- [ ] Dashboard appears after picking date
- [ ] Onboarding tour fires once
- [ ] Sign-specific palette tints the UI

### 🟡 Daily content flow

- [ ] Daily horoscope text appears on the dashboard
- [ ] Daily tip text appears in the cosmic identity section
- [ ] Question of the day card shows a non-empty question
- [ ] Question can be typed into and saved
- [ ] After save, the answered state persists across app restart
- [ ] Next morning (or after device clock advances), question is new
- [ ] Cosmic weather card shows today's sun sign + moon % + ruling planet
- [ ] Mercury retrograde flag shows during retrograde windows
- [ ] Weekly forecast shows day 1 unlocked + days 2-7 locked (free)
- [ ] Weekly forecast unlocks fully after premium purchase

### 🟡 Streak flow

- [ ] First check-in shows "1-day streak"
- [ ] Closing and reopening the app same-day doesn't double-count
- [ ] Advancing device clock by 1 day and reopening shows "2-day streak"
- [ ] Skipping a day with 0 freezes resets to "1-day streak"
- [ ] At 7-day streak, freeze badge appears with count "1"
- [ ] Skipping a day with 1+ freeze burns the freeze and preserves streak
- [ ] Streak milestone celebration fires at thresholds (1, 7, 14, 30, 100)

### 🟡 Mood journal

- [ ] Tap "Log today" → mood sheet opens
- [ ] Saving a mood persists across restart
- [ ] After 5+ entries with day-of-week pattern, MoodInsightCard appears
- [ ] Recent entries appear in the journal list

### 🟡 Birthday window

Set birth date to today/tomorrow/yesterday and verify the banner appears
in each of these windows:

- [ ] -7 to -1 days: "Your solar return is N days away"
- [ ] day 0: "Happy solar return"
- [ ] +1 to +7 days: "post-birthday glow window"
- [ ] outside ±7 days: no banner

### 🟡 Compatibility

- [ ] Browse compatibility list, tap a partner sign
- [ ] Detail screen loads, shows seed-data description + engine narrative
- [ ] Same-element pairings produce element-aware narrative
- [ ] Cross-element pairings produce element-pair-specific narrative

### 🟡 Notifications (requires keeping device on)

- [ ] Daily horoscope fires at 8am with current sun-sign content
- [ ] Mood reminder fires at 8pm
- [ ] Cosmic event notification fires when retrograde/eclipse is 1 day away
- [ ] Tapping notification opens the app to the dashboard

### 🟡 Widget

- [ ] Add widget to home screen — small, medium, large layouts render
- [ ] Cosmic age + sign show after birth date set
- [ ] Moon phase + next event populate (large layout only)
- [ ] Tap widget → opens app

### 🟡 Ads (FAN)

- [ ] On non-EEA devices: banner appears at bottom of dashboard, signs
  list, compat list, profile
- [ ] Banner does NOT appear on detail screens
- [ ] Interstitial fires at most every 90 seconds
- [ ] No ads visible during first 30 seconds of session
- [ ] Native ad slot in dashboard renders
- [ ] All ads disappear after premium purchase

### 🟡 Consent (UMP, EEA only)

- [ ] On EEA device first launch: UMP consent dialog appears
- [ ] Choosing "decline" → no FAN ads load
- [ ] Choosing "accept" → FAN ads load
- [ ] Settings → "Privacy choices" reopens the consent form
- [ ] Choice persists across app restart

### 🟡 Billing

Test on Play Console internal testing track:

- [ ] Settings → "Upgrade" → Play purchase flow appears
- [ ] Purchase completes successfully
- [ ] `obfuscatedAccountId` is set (visible in Play Console purchase log)
- [ ] After purchase, ads disappear immediately (no relaunch needed)
- [ ] Purchase is acknowledged (no auto-refund after 3 days)
- [ ] Uninstall + reinstall → purchase still recognized
- [ ] Premium card in Settings shows "PREMIUM MEMBER" state

## Play Console / store-listing prep

- [ ] `keystore.properties` filled in with real release keystore
- [ ] App signed with upload key, uploaded once to enable Play App Signing
- [ ] Privacy policy URL is current and matches data collection
  (DataStore: birthdate, mood, partners; FAN: AD_ID; Billing: Play account)
- [ ] Data Safety form filled out: declare DOB collection (PII),
  no third-party data shared, FAN/Billing as the data processors
- [ ] Target API level meets Play minimum (currently 36 — fine through 2027)
- [ ] Content rating questionnaire done
- [ ] Store listing copy from `docs/PLAY_STORE_LISTING.md`
- [ ] Screenshots: 2-8 phone screenshots minimum
- [ ] Feature graphic 1024×500
- [ ] App icon present in mipmap-anydpi-v26 + all densities — verified

## Known limitations (ship anyway, fix later)

- **Compatibility readings limit**: Currently no limit, but the upsell
  promises "Unlimited compatibility readings". Either remove that bullet
  from `PremiumUpsellSheet.kt:106` or add a per-day counter.
- **Birth chart PDF**: Mentioned in upsell, not built. Either remove the
  bullet from `PremiumUpsellSheet.kt:108` or build it post-launch.
- **English only**: `resourceConfigurations += ["en"]` in build.gradle.
  All UI strings hardcoded in Compose; horoscope/tip output is English-only.
  Localization is a v1.2+ task.
- **Mock product flavor removed**: There was a `mock` flavor in v1.0. It
  was removed alongside the networking layer. If anyone has a launcher
  shortcut to `com.hkgroups.agecalculator.mock`, it'll be a dead icon.
- **`MoodEntry` storage in DataStore string**: Capped at 30 entries.
  Mood-pattern insight quality plateaus after that. Migrate to Room
  if the feature gets traction.

## What I (Claude) actually did vs didn't

**Verified by automated tools:**
- Code compiles for debug + release
- 34 unit tests pass
- Lint passes
- No dangling references to removed APIs

**NOT verified — never ran the app:**
- UI rendering (the dashboard / engagement cards / sheets)
- Navigation flows between screens
- Notification firing
- Billing purchase
- Consent dialog
- Ad loading
- Database seeding on first launch
- Widget rendering
- Process death + recreation
- App launch under low-memory conditions

A reasonable smoke-test path before submitting to Play:

1. `./gradlew :app:assembleDebug`
2. Install on a real Android 10+ device
3. Walk the "First-launch flow" + "Daily content flow" sections above
4. If those pass, ship to internal testing track and walk
   "Notifications" + "Billing" + "Consent" with real test accounts
