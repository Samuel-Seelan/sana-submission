# Goal 7 — Setup & Team Handoff

The app is now a working Firebase app: email/password auth, Cloud Firestore for all user data
(profile, plan, sessions, recordings) and a multi-user shared-playlist feature. The exercise/injury
catalog is bundled in-app as read-only reference data; everything user-specific is in Firestore.

## 1. Firebase console steps (do these once — they can't be done from code)

The Firebase project (`sana-f6c05`) and `app/google-services.json` already exist. You still need to:

1. **Authentication** → Get started → **Sign-in method** → enable **Email/Password**.
2. **Firestore Database** → Create database → Production mode → pick a region.
3. **Firestore Database → Rules** → paste the contents of [`firestore.rules`](firestore.rules) → **Publish**.

That's it — no manual data seeding. The first account you create writes its own profile + starter plan.

## 2. Build

Open the project in **Android Studio** (it bundles a JDK) and Run. A terminal `./gradlew` build needs a
local JDK 11+ on the PATH.

## 3. Demo flow (for the slides / video)

Sign up (pick injuries) → land on Home with a curated plan → Edit playlist (add/remove, recommended
vs not-advised badges) → **Share plan** → run a **Session** (rep counter, optional ML Kit squat
counting) → **Overview** shows the day as Done → Day detail → Account (edit injuries, sign out).
Multi-user: sign in as a second account → Edit playlist → **Browse shared** → open the first user's
playlist → **Use this playlist**.

## 4. Who commits what (for an even Git history)

Isaac's Home/Session (VM + Route + Screen) are already on `main`. Commit the rest in this order so each
intermediate commit compiles.

**First — shared foundation + DB (Max / whoever set up Firebase):**
- `app/build.gradle.kts`, `build.gradle.kts`, `gradle/libs.versions.toml`, `AndroidManifest.xml`
- `model/ExerciseCatalog.kt`, `model/Recommendations.kt`
- `data/dto/FirestoreDtos.kt`
- `data/firebase/` (FirebaseRefs, FirestoreFlows, FirestoreMappers, FirebaseAuthRepository, FirebaseSanaRepository, FirebasePlaylistRepository)
- `di/AppModule.kt`, `firestore.rules`
- `repository/AuthRepository.kt` (added `changePassword`) + `data/fake/FakeAuthRepository.kt`
- VM default-repo switch in `HomeViewModel.kt` / `SessionViewModel.kt`

**Mimo — Account & Onboarding:**
- `viewmodel/AuthViewModel.kt`, `viewmodel/OnboardingViewModel.kt`, `viewmodel/AccountViewModel.kt`
- `ui/screens/onboarding/OnboardingScreen.kt`, `OnboardingRoute.kt`
- `ui/screens/account/AccountScreen.kt`, `AccountRoute.kt`

**Sam — Edit playlist, Exercise detail, Shared playlists:**
- `viewmodel/EditPlaylistViewModel.kt`, `ExerciseDetailViewModel.kt`, `SharedPlaylistsViewModel.kt`, `SharedPlaylistDetailViewModel.kt`
- `ui/screens/editplaylist/` (Screen + Route), `ui/screens/exercisedetail/` (Screen + Route)
- `ui/screens/sharedplaylists/` (Screen + Route), `ui/screens/sharedplaylistdetail/` (Screen + Route)

**Max — Overview & Day detail + final integration:**
- `viewmodel/OverviewViewModel.kt`, `viewmodel/DayDetailViewModel.kt`
- `ui/screens/overview/OverviewRoute.kt`, `ui/screens/daydetail/DayDetailRoute.kt`
- `ui/navigation/SanaNavGraph.kt`, `ui/navigation/SanaApp.kt`, `MainActivity.kt` (commit last — ties all routes together)
