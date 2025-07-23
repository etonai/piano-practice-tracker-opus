# PlayStreak Signing Certificates

This document tracks the SHA-1 fingerprints for different build variants of PlayStreak.

**NOTE:** Copy this file to `signing-certificates.md` and fill in your actual SHA-1 values. The actual file is gitignored for security.

## Debug Certificate
**Used for:** Development, Firebase setup, testing
**Keystore:** Debug keystore (auto-generated)
**SHA-1:** [PASTE YOUR DEBUG SHA-1 HERE]

## Release Certificate  
**Used for:** Production builds, Google Play Store
**Keystore:** Production keystore (to be created)
**SHA-1:** [PASTE YOUR RELEASE SHA-1 HERE WHEN CREATED]

## Notes
- Debug SHA-1 is used for Firebase initial setup
- Release SHA-1 must be added to Firebase before publishing to Play Store
- Both certificates can be registered in the same Firebase project
- Keep the actual signing-certificates.md file updated (not this template)

## Commands
- Generate signing report: `./gradlew signingReport`
- Create release keystore: `keytool -genkey -v -keystore release.keystore -alias playstreak -keyalg RSA -keysize 2048 -validity 10000`