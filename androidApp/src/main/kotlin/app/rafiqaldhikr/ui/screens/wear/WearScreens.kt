package app.rafiqaldhikr.ui.screens.wear

/**
 * Wear OS screens are defined here as references.
 * In production, Wear OS has its own module (wearApp) with Horologist/Compose for Wear.
 * These files serve as design documentation for the Wear OS companion app.
 *
 * Implementation requires:
 * 1. A separate `wearApp` module in build.gradle
 * 2. Compose for Wear OS dependency (androidx.wear.compose)
 * 3. Horologist library for standard Wear patterns
 *
 * Screens planned:
 * - WearMainScreen: Quick actions (adhkar, tasbeeh, prayer times)
 * - WearTasbeehScreen: Simple counter with haptic feedback
 * - WearPrayerScreen: Next prayer with countdown
 */

// ═══ WearMainScreen ═══
// Layout: ScalingLazyColumn with Chip items
// Actions: Morning Adhkar, Evening Adhkar, Tasbeeh, Prayer Times
// Bottom: Current streak count

// ═══ WearTasbeehScreen ═══
// Layout: Full-screen tap counter
// Center: Large count number
// Haptic: Short vibration on each tap
// Long press: Reset counter

// ═══ WearPrayerScreen ═══
// Layout: Single card showing next prayer
// Info: Prayer name, time, countdown
// Background: Color-coded by prayer (Fajr=blue, Dhuhr=gold, etc.)
