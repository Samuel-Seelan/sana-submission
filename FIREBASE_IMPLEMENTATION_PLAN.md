# Firebase Implementation Plan

This plan turns the current UI-only Sana app into a working app with Firebase-backed auth, data, progress tracking, session history, video metadata, and shared playlists.

## Current State

- The app has Compose screens, navigation, theme, UI components, and preview/sample data.
- `app/google-services.json` already exists.
- Firebase is not fully wired into Gradle yet.
- Screens currently read mostly from `SampleData`.
- There are no ViewModel, repository, or Firebase data-source layers yet.

## Target Architecture

Use a simple MVVM structure:

```text
ui/screens/*        Pure Compose screens and route wrappers
ui/navigation       Navigation graph and route constants
viewmodel/*         Screen state, events, loading/error handling
repository/*        App-facing data contracts
data/firebase/*     Firebase Auth, Firestore, Storage implementations
model/*             Domain models used by the app
data/dto/*          Firestore/storage DTOs
```

The UI should not call Firebase directly. Screens should receive state and callbacks from ViewModels. ViewModels should call repositories. Repositories should hide Firebase details.

## Shared Scaffold Now Available

The shared models, repository interfaces, fake repositories, and route names have been created so contributors can start ViewModel work before Firebase is fully implemented.

### Added Model Contracts

Backend-oriented shared models live in:

```text
app/src/main/java/com/sana/app/model/BackendModels.kt
```

Available models:

- `AuthUser`: minimal signed-in user state.
- `OnboardingProfile`: profile data collected during onboarding.
- `WorkoutSession`: saved workout session summary.
- `RecordingMetadata`: per-exercise session/video metadata.
- `SharedPlaylistSummary`: compact list item for the shared playlist page.
- `SharedPlaylist`: full shared playlist detail.

The existing UI/domain models still live in `UiModels.kt`, including:

- `Exercise`
- `InjuryProfile`
- `PlanItem`
- `Recording`
- `Milestone`
- `WeeklyStat`
- `DayStats`
- `UserProfile`

Use the existing models for screen display and previews. Use the new backend models for auth, saved sessions, recording metadata, and shared playlist workflows.

### Added Repository Interfaces

Repository contracts live in:

```text
app/src/main/java/com/sana/app/repository/AuthRepository.kt
app/src/main/java/com/sana/app/repository/SanaRepository.kt
app/src/main/java/com/sana/app/repository/PlaylistRepository.kt
```

Use these interfaces in ViewModels, not concrete fake or Firebase classes.

Example:

```kotlin
class HomeViewModel(
    private val sanaRepository: SanaRepository,
) : ViewModel() {
    val profile = sanaRepository.observeUserProfile()
    val plan = sanaRepository.observeCurrentPlan()
}
```

### Added Fake Repositories

Fake implementations live in:

```text
app/src/main/java/com/sana/app/data/fake/FakeAuthRepository.kt
app/src/main/java/com/sana/app/data/fake/FakeSanaRepository.kt
app/src/main/java/com/sana/app/data/fake/FakePlaylistRepository.kt
app/src/main/java/com/sana/app/data/fake/FakeRepositories.kt
```

Use `FakeRepositories` while building ViewModels:

```kotlin
class HomeViewModel(
    private val sanaRepository: SanaRepository = FakeRepositories.sanaRepository,
) : ViewModel()
```

This shared fake graph keeps fake state consistent across ViewModels:

- `FakeRepositories.authRepository`
- `FakeRepositories.sanaRepository`
- `FakeRepositories.playlistRepository`

For example, if the shared playlist fake copies a playlist into the current user plan, Home and Session can observe that same updated fake plan.

### Added Route Names

Shared playlist route constants were added to:

```text
app/src/main/java/com/sana/app/ui/navigation/SanaNavGraph.kt
```

Available route constants:

```kotlin
Routes.SHARED_PLAYLISTS
Routes.SHARED_PLAYLIST_DETAIL
Routes.sharedPlaylistDetail(playlistId)
```

These are only route names so far. The actual shared playlist composable destinations still need to be added after the shared playlist screens exist.

### How Contributors Should Use This

Use this pattern for each assigned screen:

1. Create a `ViewModel` in `app/src/main/java/com/sana/app/viewmodel`.
2. Inject the repository interface needed by that screen.
3. Default the repository parameter to `FakeRepositories` for now.
4. Expose one immutable UI state from the ViewModel.
5. Create a route composable that collects ViewModel state.
6. Pass plain state and callbacks into the existing screen composable.
7. Keep `SampleData` only in previews.

Example ViewModel shape:

```kotlin
data class ExampleUiState(
    val isLoading: Boolean = true,
    val error: String? = null,
)

class ExampleViewModel(
    private val sanaRepository: SanaRepository = FakeRepositories.sanaRepository,
) : ViewModel() {
    // Combine repository flows into ExampleUiState here.
}
```

Example route shape:

```kotlin
@Composable
fun ExampleRoute(
    viewModel: ExampleViewModel = viewModel(),
    onBack: () -> Unit,
) {
    val uiState by viewModel.uiState.collectAsStateWithLifecycle()

    ExampleScreen(
        uiState = uiState,
        onBack = onBack,
    )
}
```

When Firebase is ready, the team should replace the `FakeRepositories` defaults with real Firebase repository construction or dependency injection. The screen and ViewModel APIs should not need major rewrites if they only depend on repository interfaces.

## Phase 1: Firebase Project Wiring

- Confirm the Firebase project is connected to the Android package `com.sana.app`.
- Keep `app/google-services.json` in the `app/` module.
- Add the Google Services Gradle plugin.
- Add Firebase dependencies:
  - Firebase BOM
  - Firebase Auth
  - Cloud Firestore
  - Firebase Storage
- Add lifecycle dependencies for Compose ViewModel state collection:
  - `androidx.lifecycle:lifecycle-viewmodel-compose`
  - `androidx.lifecycle:lifecycle-runtime-compose`

Expected Gradle direction:

```kotlin
// root build.gradle.kts
plugins {
    alias(libs.plugins.android.application) apply false
    alias(libs.plugins.kotlin.compose) apply false
    id("com.google.gms.google-services") version "..." apply false
}
```

```kotlin
// app/build.gradle.kts
plugins {
    alias(libs.plugins.android.application)
    alias(libs.plugins.kotlin.compose)
    id("com.google.gms.google-services")
}
```

## Phase 2: Domain Models And DTOs

Keep domain models clean and app-friendly. Add DTOs for Firestore documents so Firebase serialization does not leak into UI models.

Recommended domain models:

```text
model/UserProfile.kt
model/Exercise.kt
model/InjuryProfile.kt
model/PlanItem.kt
model/WorkoutSession.kt
model/Recording.kt
model/Milestone.kt
model/SharedPlaylist.kt
```

Recommended DTOs:

```text
data/dto/UserProfileDto.kt
data/dto/ExerciseDto.kt
data/dto/InjuryProfileDto.kt
data/dto/PlanItemDto.kt
data/dto/WorkoutSessionDto.kt
data/dto/RecordingDto.kt
data/dto/SharedPlaylistDto.kt
```

Important modeling choice:

- Store `exerciseId` inside plan/session/playlist documents.
- Do not duplicate the full `Exercise` object inside every plan item.
- Join `exerciseId` to the exercise catalog in the repository.

Example shared playlist domain model:

```kotlin
data class SharedPlaylist(
    val id: String,
    val ownerUid: String,
    val ownerName: String,
    val title: String,
    val description: String,
    val injuryFocus: List<String>,
    val items: List<PlanItem>,
    val createdAtMillis: Long,
    val uses: Int,
)
```

## Phase 3: Firestore Data Shape

Use top-level public catalog collections and per-user private collections.

```text
exercises/{exerciseId}
injuryProfiles/{injuryId}

users/{uid}
  profile/private
  plan/{planItemId}
  sessions/{sessionId}
    recordings/{recordingId}
  milestones/{milestoneId}

sharedPlaylists/{playlistId}
  items/{itemId}
```

`exercises/{exerciseId}`:

```text
name
description
instructions
type
muscleGroups
difficulty
defaultReps
defaultSets
defaultDurationSec
```

`users/{uid}/profile/private`:

```text
name
email
selectedInjuryIds
createdAt
updatedAt
```

`users/{uid}/plan/{planItemId}`:

```text
exerciseId
targetReps
targetSets
targetDurationSec
order
```

`users/{uid}/sessions/{sessionId}`:

```text
startedAt
endedAt
totalReps
totalSets
totalTimeMs
exerciseCount
```

`users/{uid}/sessions/{sessionId}/recordings/{recordingId}`:

```text
exerciseId
sets
reps
durationMs
videoStoragePath
createdAt
```

`sharedPlaylists/{playlistId}`:

```text
ownerUid
ownerName
title
description
injuryFocus
createdAt
updatedAt
uses
isPublic
```

`sharedPlaylists/{playlistId}/items/{itemId}`:

```text
exerciseId
targetReps
targetSets
targetDurationSec
order
```

## Phase 4: Repository Layer

Create app-facing repository contracts first.

```text
repository/AuthRepository.kt
repository/SanaRepository.kt
repository/PlaylistRepository.kt
```

Suggested responsibilities:

`AuthRepository`:

- observe current auth user
- sign up
- sign in
- sign out

`SanaRepository`:

- observe user profile
- save onboarding profile
- observe exercise catalog
- observe injury profiles
- observe current user plan
- save user plan
- save completed session
- observe overview/progress data
- observe day detail data

`PlaylistRepository`:

- publish current playlist as shared playlist
- observe shared playlists
- observe one shared playlist
- copy shared playlist into current user plan
- increment shared playlist use count

## Phase 5: Firebase Data Sources

Create Firebase implementations behind the repository contracts.

```text
data/firebase/FirebaseAuthRepository.kt
data/firebase/FirebaseSanaRepository.kt
data/firebase/FirebasePlaylistRepository.kt
data/firebase/FirebaseRefs.kt
```

`FirebaseRefs` should centralize collection paths:

```kotlin
class FirebaseRefs(private val db: FirebaseFirestore) {
    val exercises = db.collection("exercises")
    val injuryProfiles = db.collection("injuryProfiles")
    val sharedPlaylists = db.collection("sharedPlaylists")

    fun userProfile(uid: String) =
        db.collection("users").document(uid)
            .collection("profile").document("private")

    fun userPlan(uid: String) =
        db.collection("users").document(uid).collection("plan")
}
```

## Phase 6: ViewModels

Add one ViewModel per major workflow.

```text
viewmodel/AuthViewModel.kt
viewmodel/OnboardingViewModel.kt
viewmodel/HomeViewModel.kt
viewmodel/EditPlaylistViewModel.kt
viewmodel/SessionViewModel.kt
viewmodel/OverviewViewModel.kt
viewmodel/DayDetailViewModel.kt
viewmodel/ExerciseDetailViewModel.kt
viewmodel/AccountViewModel.kt
viewmodel/SharedPlaylistsViewModel.kt
viewmodel/SharedPlaylistDetailViewModel.kt
```

Each ViewModel should expose a single immutable UI state.

Example:

```kotlin
data class SharedPlaylistsUiState(
    val isLoading: Boolean = true,
    val playlists: List<SharedPlaylistSummary> = emptyList(),
    val error: String? = null,
)
```

Use events for actions:

```kotlin
fun publishCurrentPlan(title: String, description: String)
fun useSharedPlaylist(playlistId: String)
fun refresh()
```

## Phase 7: Route Wrappers

Keep existing screen composables mostly pure. Add route composables that connect ViewModels to screens.

Example structure:

```text
ui/screens/home/HomeRoute.kt
ui/screens/home/HomeScreen.kt
ui/screens/sharedplaylists/SharedPlaylistsRoute.kt
ui/screens/sharedplaylists/SharedPlaylistsScreen.kt
ui/screens/sharedplaylistdetail/SharedPlaylistDetailRoute.kt
ui/screens/sharedplaylistdetail/SharedPlaylistDetailScreen.kt
```

The route owns:

- ViewModel lookup
- `collectAsStateWithLifecycle`
- navigation side effects
- snackbar/error handling

The screen owns:

- layout
- buttons
- text fields
- lists
- loading/empty/error visuals

## Phase 8: Authentication Flow

Update navigation to start from auth state instead of always starting at Home.

Flow:

```text
Launch app
  -> Auth loading
  -> signed out: Onboarding
  -> signed in: Home
```

Onboarding should support:

- create account
- sign in
- user name
- injury selection
- initial generated plan
- save profile to Firestore

Account should support:

- display profile
- sign out
- maybe edit name/injuries later

## Phase 9: Playlist Editing

Make `EditPlaylistScreen` read and write the user plan through `EditPlaylistViewModel`.

Core behavior:

- Load exercise catalog.
- Load current user plan.
- Show recommended exercises based on selected injuries.
- Add exercise to plan.
- Remove exercise from plan.
- Reorder exercises.
- Save plan to `users/{uid}/plan`.

Keep `SampleData` only for previews.

## Phase 10: Shared Playlist Feature

Add a feature that lets users publish their plan and use plans shared by others.

### New Navigation Routes

```kotlin
object Routes {
    const val SHARED_PLAYLISTS = "shared_playlists"
    const val SHARED_PLAYLIST_DETAIL = "shared_playlist/{playlistId}"

    fun sharedPlaylistDetail(playlistId: String) = "shared_playlist/$playlistId"
}
```

### New Screens

`SharedPlaylistsScreen`:

- list public shared playlists
- search/filter by injury focus
- show title, owner, exercise count, injury focus, use count
- open playlist detail

`SharedPlaylistDetailScreen`:

- show playlist title, owner, description
- show full exercise list and targets
- button: `Use playlist`
- optional button: `Save as copy`

### Entry Points

Add access from:

- Home screen: browse shared playlists
- Edit playlist screen: publish current playlist
- Edit playlist screen: browse shared playlists

### Publish Playlist Flow

From `EditPlaylistScreen`:

```text
Tap Share/Publish
  -> enter title and description
  -> choose injury focus tags from selected injuries or catalog
  -> publish
  -> create sharedPlaylists/{playlistId}
  -> create sharedPlaylists/{playlistId}/items
```

### Use Shared Playlist Flow

From `SharedPlaylistDetailScreen`:

```text
Tap Use playlist
  -> confirm replace current playlist
  -> copy shared playlist items to users/{uid}/plan
  -> increment sharedPlaylists/{playlistId}.uses
  -> navigate to Home or Edit playlist
```

Start with replace behavior. Add merge behavior later if needed.

### Shared Playlist Rules

First version:

- Users can read public shared playlists.
- Authenticated users can create shared playlists.
- Only the owner can update or delete their shared playlist.
- Authenticated users can copy any public shared playlist into their own plan.

## Phase 11: Sessions And Progress

Make `SessionViewModel` own the session state currently held in `remember`.

When a session finishes:

- calculate total reps
- calculate total sets
- calculate duration
- save session document
- save per-exercise recording metadata
- update milestones locally or in repository

For video:

- first save metadata with `hasClip = false`
- upload video/clip to Firebase Storage
- update `videoStoragePath`
- set `hasClip = true`

Storage path:

```text
users/{uid}/sessions/{sessionId}/recordings/{recordingId}.mp4
```

## Phase 12: Overview And Detail Pages

`OverviewViewModel` should observe sessions and derive:

- this week days
- previous weeks
- weekly totals
- milestones

`DayDetailViewModel` should load:

- sessions for selected day
- recordings for those sessions
- day stat totals

`ExerciseDetailViewModel` should load:

- exercise details
- user history for that exercise
- recommendation/block status
- related recordings

## Phase 13: Security Rules

Firestore starting rules:

```js
rules_version = '2';
service cloud.firestore {
  match /databases/{database}/documents {
    function signedIn() {
      return request.auth != null;
    }

    function owns(userId) {
      return signedIn() && request.auth.uid == userId;
    }

    match /exercises/{exerciseId} {
      allow read: if signedIn();
      allow write: if false;
    }

    match /injuryProfiles/{injuryId} {
      allow read: if signedIn();
      allow write: if false;
    }

    match /users/{userId}/{document=**} {
      allow read, write: if owns(userId);
    }

    match /sharedPlaylists/{playlistId} {
      allow read: if signedIn() && resource.data.isPublic == true;
      allow create: if signedIn()
        && request.resource.data.ownerUid == request.auth.uid;
      allow update, delete: if signedIn()
        && resource.data.ownerUid == request.auth.uid;

      match /items/{itemId} {
        allow read: if signedIn();
        allow create, update, delete: if signedIn()
          && get(/databases/$(database)/documents/sharedPlaylists/$(playlistId)).data.ownerUid == request.auth.uid;
      }
    }
  }
}
```

Storage starting rules:

```js
rules_version = '2';
service firebase.storage {
  match /b/{bucket}/o {
    match /users/{userId}/{allPaths=**} {
      allow read, write: if request.auth != null
        && request.auth.uid == userId;
    }
  }
}
```

## Phase 14: Seeding Data

Move the current `SampleData` exercise and injury catalog into Firestore.

Options:

- Manual Firebase console entry for a small demo.
- One-time Kotlin debug-only seeding function.
- Admin SDK script outside the app.

Recommended for this project:

- Keep `SampleData` for previews.
- Create a simple debug-only seeding path later if manual entry becomes annoying.

## Phase 15: Testing Plan

Unit tests:

- DTO/domain mapping
- repository logic with fake data sources
- ViewModel state transitions
- shared playlist copy behavior
- session total calculations

Manual tests:

- new user onboarding saves profile and plan
- existing user lands on Home
- edit playlist persists after app restart
- publish playlist creates public playlist
- another account can view shared playlist
- using shared playlist replaces current user plan
- session completion appears in overview
- sign out returns to onboarding

## Contributor Work Split

This split keeps each contributor focused on their assigned screens while sharing the model, repository, and Firebase contracts. The shared contracts should be agreed on first so screen work can happen in parallel.

### Shared Setup For Everyone

Owner: whole team, ideally completed first together.

- Add Firebase Gradle dependencies and confirm the app builds.
- Create shared package structure:
  - `model`
  - `data/dto`
  - `repository`
  - `data/firebase`
  - `viewmodel`
- Agree on domain models:
  - `UserProfile`
  - `Exercise`
  - `InjuryProfile`
  - `PlanItem`
  - `WorkoutSession`
  - `Recording`
  - `Milestone`
  - `SharedPlaylist`
- Agree on repository interfaces:
  - `AuthRepository`
  - `SanaRepository`
  - `PlaylistRepository`
- Agree on route naming for new shared playlist pages.
- Keep `SampleData` available for previews only.

Dependencies:

- All contributor ViewModels depend on the shared domain models.
- All Firebase-backed screens depend on repository interfaces existing, even if the first implementation is fake.
- Shared playlist screens depend on `Exercise`, `PlanItem`, `SharedPlaylist`, and `PlaylistRepository`.

### 1. Mimo: Account And Onboarding

Primary files:

- `ui/screens/onboarding/OnboardingScreen.kt`
- `ui/screens/account/AccountScreen.kt`
- `viewmodel/AuthViewModel.kt`
- `viewmodel/OnboardingViewModel.kt`
- `viewmodel/AccountViewModel.kt`
- `repository/AuthRepository.kt`
- auth-related methods in `SanaRepository`

Tasks:

- Implement Firebase Auth sign up, sign in, and sign out.
- Make app startup depend on auth state instead of starting directly at Home.
- Save user profile to `users/{uid}/profile/private`.
- Save selected injury IDs during onboarding.
- Create initial user plan after onboarding.
- Load and display account profile data.
- Add account sign-out flow that returns to onboarding.
- Add loading and error states for auth actions.

Dependencies:

- Depends on shared Firebase Gradle setup.
- Depends on `UserProfile`, `InjuryProfile`, and `PlanItem` models.
- Depends on `SanaRepository.observeInjuryProfiles()` for onboarding injury selection.
- Isaac's Home work depends on Mimo's auth state so Home knows whether a user is signed in.
- Sam's recommendation logic depends on Mimo saving selected injury IDs correctly.

Deliverable:

- A user can create an account, choose injuries, save a profile, sign out, and sign back in.

### 2. Sam: Edit Playlist And Exercise Detail

Primary files:

- `ui/screens/editplaylist/EditPlaylistScreen.kt`
- `ui/screens/exercisedetail/ExerciseDetailScreen.kt`
- `viewmodel/EditPlaylistViewModel.kt`
- `viewmodel/ExerciseDetailViewModel.kt`
- playlist methods in `SanaRepository`
- `repository/PlaylistRepository.kt`
- `data/firebase/FirebasePlaylistRepository.kt`

Tasks:

- Load exercise catalog from Firestore.
- Load current user's playlist from `users/{uid}/plan`.
- Add, remove, and reorder playlist exercises.
- Save playlist changes to Firestore.
- Show recommended and blocked exercises based on selected injuries.
- Load exercise detail by `exerciseId`.
- Show exercise history if session data is available.
- Add publish/shared playlist entry point from Edit Playlist.
- Implement publish current playlist flow:
  - title
  - description
  - injury focus tags
  - publish to `sharedPlaylists/{playlistId}`
  - publish items to `sharedPlaylists/{playlistId}/items`

Dependencies:

- Depends on shared `Exercise`, `PlanItem`, and `SharedPlaylist` models.
- Depends on Mimo's onboarding/profile work for selected injury IDs.
- Depends on Firebase catalog seeding for real exercise data.
- Isaac's Home and Session work depend on Sam's saved user plan.
- Shared playlist detail page depends on Sam's `PlaylistRepository.observeSharedPlaylist()`.

Deliverable:

- A user can edit their playlist, persist it, view exercise details, and publish their playlist for others.

### 3. Max: Overview And Day Detail

Primary files:

- `ui/screens/overview/OverviewScreen.kt`
- `ui/screens/daydetail/DayDetailScreen.kt`
- `viewmodel/OverviewViewModel.kt`
- `viewmodel/DayDetailViewModel.kt`
- session/progress methods in `SanaRepository`

Tasks:

- Observe completed sessions from Firestore.
- Derive this week's day cells from saved sessions.
- Derive previous week rows.
- Derive weekly progress stats.
- Load sessions and recordings for a selected day.
- Calculate day totals:
  - total reps
  - total sets
  - total time
  - exercises done
- Show empty states for days with no completed sessions.
- Connect Day Detail exercise taps to Exercise Detail.

Dependencies:

- Depends on shared `WorkoutSession`, `Recording`, `WeeklyStat`, and `DayStats` models.
- Depends on Isaac's Session work to create real session documents.
- Depends on Sam's exercise catalog work to turn recording `exerciseId` values into exercise names/details.
- Can start early with fake repository data while Isaac finishes session persistence.

Deliverable:

- Overview and Day Detail show real progress and session history once sessions are saved.

### 4. Isaac: Home And Session

Primary files:

- `ui/screens/home/HomeScreen.kt`
- `ui/screens/session/SessionScreen.kt`
- `viewmodel/HomeViewModel.kt`
- `viewmodel/SessionViewModel.kt`
- session methods in `SanaRepository`
- navigation entry points for shared playlist pages

Tasks:

- Load profile summary for Home.
- Load today's/current user playlist for Home.
- Add Home entry point to shared playlists.
- Add Home entry points to session, overview, account, edit playlist, and exercise detail using real state.
- Move `SessionScreen` state from `remember` into `SessionViewModel`.
- Track current exercise, reps, sets, phase, and duration.
- Save completed session to `users/{uid}/sessions/{sessionId}`.
- Save per-exercise recording metadata under the session.
- Add placeholder support for future Firebase Storage video upload.
- Add session completion summary from real session totals.

Dependencies:

- Depends on Mimo's auth flow so Home only loads for a signed-in user.
- Depends on Sam's playlist persistence so Session has real plan items.
- Max's Overview and Day Detail depend on Isaac saving session documents consistently.
- Shared playlist browse entry point depends on shared playlist routes being added.

Deliverable:

- Home shows real user/plan data, and Session saves completed workout history to Firestore.

### Shared Playlist Page Ownership

Recommended owner: Sam, because it is closest to playlist editing. Isaac should add the Home navigation entry point.

Files:

- `ui/screens/sharedplaylists/SharedPlaylistsScreen.kt`
- `ui/screens/sharedplaylists/SharedPlaylistsRoute.kt`
- `ui/screens/sharedplaylistdetail/SharedPlaylistDetailScreen.kt`
- `ui/screens/sharedplaylistdetail/SharedPlaylistDetailRoute.kt`
- `viewmodel/SharedPlaylistsViewModel.kt`
- `viewmodel/SharedPlaylistDetailViewModel.kt`

Tasks:

- List public shared playlists.
- Filter/search by injury focus.
- Open shared playlist detail.
- Show playlist owner, title, description, exercises, targets, and use count.
- Copy shared playlist into the current user's plan.
- Increment use count after copy.
- Navigate back to Home or Edit Playlist after using a shared playlist.

Dependencies:

- Depends on `PlaylistRepository`.
- Depends on exercise catalog loading so playlist items can display exercise names.
- Depends on Mimo's auth work because only signed-in users can use shared playlists.
- Depends on Sam's user plan save behavior because using a playlist replaces the user's plan.

Deliverable:

- A signed-in user can browse shared playlists and use one as their current playlist.

### Parallel Work Strategy

Use fake repository implementations first if Firebase implementation is not ready. This lets everyone convert screens to ViewModels in parallel.

Suggested parallel order:

1. Team creates shared models, repository interfaces, route names, and fake repositories.
2. Mimo builds auth/onboarding/account against interfaces.
3. Sam builds playlist editing and shared playlist publishing against interfaces.
4. Isaac builds Home/Session against fake then real plan/session repositories.
5. Max builds Overview/Day Detail against fake session data, then switches to Isaac's saved session data.
6. Team swaps fake repositories for Firebase implementations.
7. Team verifies cross-screen flows together.

Critical dependency chain:

```text
Firebase setup
  -> shared models/repository interfaces
  -> auth/profile onboarding
  -> user plan persistence
  -> home/session using real plan
  -> session persistence
  -> overview/day detail from real sessions
```

Shared playlist dependency chain:

```text
shared models/repository interfaces
  -> exercise catalog + user plan persistence
  -> publish shared playlist
  -> shared playlist list/detail
  -> copy shared playlist into user plan
  -> Home/Session use copied playlist
```

## Suggested Build Order

1. Wire Firebase Gradle dependencies.
2. Add domain models and DTO mapping.
3. Add Auth repository and `AuthViewModel`.
4. Make onboarding create/sign in users.
5. Add `SanaRepository` and load profile/catalog/plan.
6. Convert Home and Edit Playlist to ViewModels.
7. Add shared playlist repository.
8. Add Shared Playlists list page.
9. Add Shared Playlist detail page and `Use playlist`.
10. Add publish playlist flow.
11. Convert Session to ViewModel and save completed sessions.
12. Convert Overview, Day Detail, and Exercise Detail to real data.
13. Add security rules.
14. Add tests and clean up remaining `SampleData` runtime usage.

## Definition Of Done

- App starts based on real auth state.
- New users can onboard and save selected injuries.
- Users can edit and persist their own playlist.
- Users can publish a playlist.
- Users can browse shared playlists from other users.
- Users can copy a shared playlist into their own plan.
- Completed sessions are saved to Firestore.
- Overview and detail pages show real saved progress.
- Firebase rules prevent users from reading/writing another user's private data.
- `SampleData` remains only for previews and local UI demos.
