# SoftPOS Simulator

Standalone Android app that simulates the external SoftPOS application expected by the STC Digital Sales app.

It supports these implicit intent actions with MIME type `text/plain`:

- `geidea.net.softpos.REGISTRATION_STATUS`
- `geidea.net.softpos.LASTTRANSACTIONDETAILS`
- `geidea.net.softpos.REFUND`
- `geidea.net.softpos.PURCHASE`
- `geidea.net.softpos.REVERSAL`

## What it does

- Opens from the launcher or the Android chooser.
- Shows the incoming action, MIME type, and extras.
- Prefills outgoing values from request extras when available.
- Builds result intents with the exact top-level extras the STC app reads: `status`, `data`, `result`, and `reason`.
- Validates `terminal_id` length and other contract-sensitive fields before returning.
- Includes a compatibility flavor whose `applicationId` is `com.riyad.softpos`.

## Project structure

- `SoftPosSimulatorActivity`: single exported entry activity.
- `model/`: action enums, form data, and UI state.
- `payload/`: pure Kotlin payload generation and result intent creation.
- `ui/`: intent parsing, defaults, ViewModel, and Compose screen.
- `app/src/test/`: payload unit tests.

## Setup

1. Open the project in Android Studio.
2. Let Gradle sync and install any missing SDK packages for API 36 if prompted.
3. Build one of these variants:
   - `standardDebug`
   - `riyadCompatDebug`
4. Install the APK on a device or emulator.

## Recommended variants

- `standardDebug`: normal simulator package `sa.com.stc.softpossimulator`
- `riyadCompatDebug`: compatibility package `com.riyad.softpos`

Use `riyadCompatDebug` if the STC app also checks whether package `com.riyad.softpos` is installed before launching the flow.

## Example usage

1. Launch the STC app flow that opens the chooser for one of the SoftPOS actions.
2. Pick this simulator from the chooser.
3. Review the incoming request section.
4. Adjust the outgoing payload if needed.
5. Tap `Return Result`.

You can also open the simulator from the launcher and manually choose any SoftPOS action to preview the result payload.

## Contract notes

- Registration success returns `status = "Success"` and puts the JSON inside `data`.
- Purchase, refund, and reversal use the exact case-sensitive status values the STC app checks: `Approved`, `Declined`, `Aborted`.
- `LASTTRANSACTIONDETAILS` success stringifies the inner `message` JSON exactly as the consumer expects.
- `terminal_id` is blocked when shorter than 8 characters.

## Useful adb commands

Install standard debug:

```bash
./gradlew installStandardDebug
```

Install Riyad compatibility debug:

```bash
./gradlew installRiyadCompatDebug
```

Launch purchase manually:

```bash
adb shell am start \
  -a geidea.net.softpos.PURCHASE \
  -t text/plain \
  --es android.intent.extra.TEXT "115.00" \
  --es ORDER_ID "ORDER-1001"
```

Launch refund manually:

```bash
adb shell am start \
  -a geidea.net.softpos.REFUND \
  -t text/plain \
  --es android.intent.extra.TEXT "115.00" \
  --es ORDER_ID "ORDER-1001" \
  --es ORIGINAL_TRANS_SEQ_NO "000123" \
  --es ORIGINAL_TRANS_DATE "17-04-2026"
```

Launch reversal manually:

```bash
adb shell am start \
  -a geidea.net.softpos.REVERSAL \
  -t text/plain \
  --es ORDER_ID "ORDER-1001"
```

Launch registration status manually:

```bash
adb shell am start \
  -a geidea.net.softpos.REGISTRATION_STATUS \
  -t text/plain
```

Launch last transaction manually:

```bash
adb shell am start \
  -a geidea.net.softpos.LASTTRANSACTIONDETAILS \
  -t text/plain
```

Run unit tests:

```bash
./gradlew testStandardDebugUnitTest
```
