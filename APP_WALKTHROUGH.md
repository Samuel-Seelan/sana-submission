# Sana App Walkthrough

Sana is a physiotherapy recovery companion that creates an injury-aware exercise plan, guides the
user through recorded workout sessions, and tracks progress over time. This walkthrough covers the
implemented app from first launch through account management.

## Before starting

- Run the Android app on a device or emulator with internet access.
- Ensure the Firebase configuration is available so authentication and Firestore features work.
- A camera-enabled device gives the best session experience.
- Allow camera and microphone access when prompted if you want to record a workout.

## 1. Create an account or log in

1. Launch Sana. The app checks the current Firebase authentication state.
2. If signed out, the **Sign up / Log in** screen appears.
3. To create an account, remain on **Sign up** and enter your name, email address, and a password
   containing at least six characters.
4. Select any injuries that should influence your recovery plan.
5. Tap **Sign up**.

Sana creates the Firebase account, stores the profile and selected injuries in Firestore, and seeds
a recommended starter plan. Exercises that may be unsafe for the selected injuries are marked as
not advised.

To use an existing account instead, choose **Log in**, enter the email and password, and tap
**Log in**. Successful authentication automatically opens the signed-in app.

## 2. Explore the Home screen

1. Review the personalized greeting and **Today's Workout** carousel.
2. Swipe through the exercises in the current plan.
3. Tap an exercise card to open its complete exercise details.
4. Review the weekly progress chart, which compares weekly repetitions and active minutes.
5. Review earned milestones generated from saved workout sessions.

If the plan is empty, Sana displays an empty-plan message and an **Edit playlist** button.

The Home screen also provides four main actions:

- **Start** begins a guided workout.
- **Edit** opens the plan editor.
- **Full plan overview** opens the progress calendar.
- The profile icon opens account settings.

## 3. Review an exercise

1. Open an exercise from Home, the plan editor, a shared playlist, or a recorded day.
2. Watch the embedded YouTube demonstration when one is configured. Exercises without a video use
   bundled artwork or a labeled fallback illustration.
3. Review the muscle group, difficulty, type, target sets/repetitions or duration, and step-by-step
   instructions.
4. Check the injury-aware banner. It identifies exercises as recommended or not advised based on
   the injuries saved in the profile.
5. Review previous sets, repetitions, duration, and clip availability for that exercise.
6. Tap **Add to plan** if the exercise is not already included.

## 4. Build and save a workout plan

1. From Home, tap **Edit**.
2. Review the current plan at the top of the screen.
3. Tap the **X** on a plan card to remove that exercise.
4. Press and hold the drag handle, then drag a card to reorder the plan.
5. Browse the grouped catalog or use **Search exercises** to find an exercise by name.
6. Enable **My injuries only** to focus on injury-relevant recommendations.
7. Select a muscle-group filter to narrow the catalog further.
8. Use the score badge to distinguish recommended and not-advised exercises.
9. Tap **+** on a catalog card to add it to the working plan. A check mark indicates that it is
   already included.
10. Tap **Save** to persist the plan to Firestore.

Changes remain a working draft until **Save** is tapped. The saved order becomes the order used by
the guided session.

## 5. Publish or use a shared playlist

### Publish the current plan

1. In the plan editor, tap **Share plan**.
2. Enter a required title and an optional description.
3. Tap **Publish**.

Sana first saves the current plan, then publishes a public copy containing its exercises and injury
focus.

### Browse community plans

1. In the plan editor, tap **Browse shared**.
2. View each playlist's owner, description, injury focus, exercise count, and use count.
3. Choose **All** or an injury filter to narrow the list.
4. Tap a playlist to open its details.
5. Tap an exercise in the list to inspect it before using the plan.
6. Tap **Use this playlist** and confirm **Use playlist**.

Using a shared playlist replaces the user's current plan with that playlist and returns to Home.
The shared playlist's use count is incremented.

## 6. Complete a guided workout

1. From Home, tap **Start**.
2. Choose whether to enable automatic rep counting.
   - **Enable** activates ML Kit pose detection for squat exercises.
   - **Not now** keeps manual rep controls available.
3. Grant camera and microphone permission when requested.
4. Tap **Start session**. CameraX begins one continuous local video recording for the session.
5. During the rest phase, review the next exercise and its target.
6. Tap **Start exercise** when ready.
7. Follow the exercise demonstration and use the live camera preview to monitor positioning.
8. Track repetitions:
   - Use **+** and **−** for manual adjustments.
   - For squat exercises with automatic counting enabled, move fully into frame and follow the pose
     status. The pose skeleton and squat counter update from the camera feed.
9. Tap **Complete set** after each set. Sana stores the set and rep totals, then resets the current
   set counter.
10. Tap **End exercise & next** to advance, or **End exercise & finish** on the final exercise.
11. Use **End session early** if necessary; completed work is still summarized and saved.
12. Review total repetitions, sets, active time, completed exercises, and newly earned milestones.
13. Tap **Back to Home**.

When the session finishes, Sana stops the recording, stores the video locally, and writes the
session and per-exercise recording metadata to Firestore.

## 7. Review progress and workout history

1. From Home, tap **Full plan overview**.
2. Review the current Monday-to-Sunday strip and the recovery-week history.
3. Use the status colors to distinguish completed, partial, missed, future, and rest days.
4. Tap a day containing activity to open **Day detail**.
5. Review the day's total repetitions, sets, active time, and number of exercises.
6. Swipe through the exercise recordings from that day.
7. Tap a recording to open its replay dialog, then tap **Exercise details** to revisit its
   instructions and history.

Recorded-clip replay currently uses a labeled placeholder surface. The app stores local video paths
and Firestore metadata, but full playback or cloud video upload is not yet implemented.

## 8. Manage the account

1. Tap the profile icon on Home.
2. Edit the name or email and tap **Save profile**.
3. Add or remove injury selections and tap **Save injuries**.
4. Enter a new password of at least six characters and tap **Change password**.
5. Tap **Sign out** when finished.

Profile and injury changes are stored in Firestore. Updated injuries immediately affect exercise
recommendations and safety warnings. Signing out changes the global authentication state and
returns the app to the onboarding screen.

## Implemented functionality checklist

- Firebase email/password sign-up, login, password change, and sign-out
- Firestore-backed user profiles and injury selections
- Injury-aware starter plans, recommendations, and safety warnings
- Searchable and filterable exercise catalog
- Add, remove, reorder, and persist plan exercises
- Public playlist publishing, browsing, filtering, detail, and adoption
- Embedded exercise demonstrations with image fallbacks
- Guided multi-exercise sessions with manual rep/set tracking
- CameraX preview and continuous local workout recording
- Optional ML Kit squat pose overlay and automatic rep counting
- Firestore session and recording-metadata persistence
- Weekly charts, milestones, plan overview, daily totals, and exercise history

## Current limitation

Full recorded-video playback and cloud video upload are not implemented. All other flows above are
wired to the current app behavior.
