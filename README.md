# Sana — UI Skeleton

Sana is a physiotherapy recovery companion: pick your injuries, follow a curated set of
exercises, record guided sessions for form review, and track progress over time.

This repository is the **UI skeleton** for the app (Goal 6). Every intended screen is built
as a Jetpack Compose screen with a scaffold, reusable components, and `@Preview`s driven by
sample data. There is **no backend** yet — no database, networking, camera, or video
playback. Those are represented by lightweight placeholders so the full screen flow can be
navigated and previewed.

## Screens

| Screen | Route | Purpose |
| --- | --- | --- |
| Onboarding | `onboarding` | Sign up / log in, choose injuries |
| Home | `home` | Today's workout, start/edit, progress chart |
| Account | `account` | Edit profile, injuries, password, sign out |
| Edit playlist | `edit_playlist` | Add/remove exercises from the plan |
| Session | `session` | Guided session (demo video + camera placeholders, rep counter) |
| Overview | `overview` | Calendar of recovery weeks |
| Day detail | `day/{epochDay}` | One day's recorded exercises and stats |
| Exercise detail | `exercise/{exerciseId}` | Demo, instructions, recommendation, recordings |

## Project layout

```
app/src/main/java/com/sana/app/
├── MainActivity.kt            // entry point, applies theme + nav graph
├── model/
│   ├── UiModels.kt            // plain UI data classes (no persistence)
│   ├── SampleData.kt          // dummy data for previews and the runnable skeleton
│   └── Display.kt             // small formatting helpers
├── ui/
│   ├── theme/                 // Sana dark color scheme, typography, theme
│   ├── components/            // reusable building blocks
│   │   ├── Common.kt          // SectionHeader, EmptyState, StatChip, formatDuration
│   │   ├── ExerciseCard.kt    // gradient-thumbnail exercise card + badges
│   │   ├── ProgressChart.kt   // weekly reps/time chart (Canvas)
│   │   └── MediaPlaceholders.kt // video & camera placeholders (no Media3/CameraX)
│   ├── navigation/
│   │   └── SanaNavGraph.kt    // all routes wired together
│   └── screens/               // one package per screen, each with @Preview(s)
```

## Building

Open the project in Android Studio and run the `app` configuration on a device or emulator
(min SDK 28). To browse the UI without running, open any screen file and use the Compose
preview pane.
