# Brag Plan: Poco F5 Tweaker

## What is this app?
Poco F5 Tweaker is an open-source Android hardware and kernel tuning suite for the Snapdragon 7+ Gen 2 (codename: `marble`), applying locked 120Hz refresh rates, Vulkan rendering, and MIUI thermal limit overrides via Shizuku ADB without root.

## The angle
Your phone has elite flagship silicon, but MIUI locked it down in thermal handcuffs. Poco F5 Tweaker uncages the raw hardware with Apple HIG precision and one-tap profiles—no bootloader unlock, no Magisk, zero root.

## Hook (first 2-3 seconds)
A stark black screen with bold technical copy: "Your Snapdragon 7+ Gen 2 is throttled." An ADB terminal ping pulses green: `Shizuku Service: CONNECTED`.

## Key moments (the middle)
- **The Master Profile switch:** Tap "Performance" (120Hz locked, CPU boost, Dalvik 384m heap) snapping instantly into place.
- **The forbidden toggle:** Flip "Disable Joyose" with its red "Caution" badge ("Bypasses MIUI thermal throttling. Active cooling recommended").
- **Hardware stack ignition:** Touch boost digitizer, Vulkan SkiaVK UI acceleration, and Multicore Packet Scheduler lighting up in rapid sequence.

## Outro / punchline
"Uncage marble. Zero root required." Slams into the Poco F5 Tweaker icon, sleek typography, and GitHub link.

## User flow worth showing
1. Entry: Shizuku status badge links with a crisp green ping (`Shizuku Active`).
2. Action: One tap switches Global Preset to "Performance" (120Hz • Max Boost).
3. Action: User toggles "Disable Joyose" (thermal limits bypassed).
4. Result: Immediate live hardware stats confirmed: 120Hz locked, SkiaVK active, Joyose terminated.

## Tone
- Preset: `app-store`
- Creative direction: High-performance mobile hardware launch reel
- Interpretation: Crisp, premium Apple HIG visual hierarchy meets unhinged Android power-user performance tuning; confident, fast, and legible.

## Format: landscape — 1920x1080
## Duration: 18.5 seconds

## Visual identity (from the project)
- Background: `#000000` (deep OLED dark) / `#1C1C1E` (Apple HIG dark surface)
- Accent: `#30D158` (System Green), `#0A84FF` (System Blue), `#FF9F0A` (System Orange), `#FF453A` (System Red)
- Text: `#FFFFFF` (Primary Text), `#8E8E93` (Secondary Text), `#636366` (Tertiary Text)
- Display font: `system-ui, -apple-system, BlinkMacSystemFont, "SF Pro Display", Roboto, sans-serif`
- Body font: `system-ui, -apple-system, BlinkMacSystemFont, "SF Pro Text", Roboto, sans-serif`
- Strongest visual element: Inset grouped card layout with squircle icon badges, segmented profile buttons, and glowing Shizuku status pill.

## Share copy (draft)
Uncaged the Snapdragon 7+ Gen 2: Poco F5 Tweaker unlocks 120Hz locked displays, Vulkan SkiaVK rendering, and MIUI thermal limit overrides via Shizuku ADB with zero root.

## Audio direction
- Role: Rhythmic electronic bed with crisp tactile interface accents
- Music: `happy-beats-business-moves-vol-11-by-ende-dot-app.mp3`
- Music treatment: High clarity, starting at t=0s, volume 0.35, gentle ducking during key text reveals, smooth outro fade
- Music cue guidance: Preset file read from `assets/music/cues/happy-beats-business-moves-vol-11-by-ende-dot-app.music-cues.md`; tempo 114.84 BPM; strong cues at 1.60s, 3.70s, 5.80s, 8.96s, 12.65s, 17.91s
- Audio-reactive treatment: Subtle; background ambient aura and card borders subtly breathe with music bass
- SFX posture: Moderate, precise, tactile UI clicks and soft impact drops
- Audio-coupled moments:
  - Shizuku pill activation: crisp select click
  - Master profile tap: tactile switch sound
  - Joyose toggle: soft warning impact
  - Final branding lock: resonant impact bell
- Restraint rule: No distorted clipping, no cartoon noises; retain clean hardware tech aesthetic

## Storyboard

### Scene 1 — The Hook & Shizuku Link — 3.7s
- Black OLED canvas. Bold headline emerges: "Your Snapdragon 7+ Gen 2 is throttled."
- At 1.60s (beat-locked), the Shizuku status card slides in: "Shizuku Service Detected" with a pulsing green dot turning "Active (ADB Authorized)".
- Sequential/interaction: Shizuku pill connects and illuminates green.
- Audio intent: Precision connection, tech awakening.
- Audio-coupled idea: Crisp connection tap at 1.60s.
- Music: Electronic groove builds from 0s.
- Transition mood: Clean slide right → Scene 2 (at 3.70s, beat-locked).

### Scene 2 — Master Profile Override — 5.3s (3.7s – 9.0s)
- The Poco F5 Tweaker main dashboard appears featuring the Inset Grouped "GLOBAL PRESET" card.
- Three segmented options visible: "Battery (60Hz)", "Balanced (90Hz)", "Performance (120Hz • Max Boost)".
- Simulated cursor/finger taps "Performance" at 5.80s (beat-locked): card glows orange, sub-status updates to "120Hz Locked • Dalvik 384m • Scheduler Boosted".
- Sequential/interaction: Simulated tap on "Performance", status values flip in sequence.
- Audio intent: Satisfying power-up tactile engagement.
- Audio-coupled idea: `ui/click1.ogg` on profile tap at 5.80s, soft card slide `casino/card-slide-1.ogg`.
- Transition mood: Fast wipe → Scene 3 (at 8.96s / 9.0s, beat-locked).

### Scene 3 — Bypassing Thermal Guardrails (Joyose) — 5.0s (9.0s – 14.0s)
- Direct view of "HARDWARE & SYSTEM TOGGLES" Inset Grouped Card.
- Row: "Disable Joyose" with Red "Caution" Badge: "Bypasses MIUI thermal throttling. Active cooling strongly recommended."
- Switch toggles to ON at 10.54s (beat-grid); alert banner flashes softly: "Thermal Guardrails Deactivated".
- Two companion rows flip in sequence: "Vulkan UI Renderer (SkiaVK)" and "Touch Responsiveness Boost".
- Sequential/interaction: Switches flip sequentially at 10.54s, 11.60s, 12.65s.
- Audio intent: Edge-of-your-seat power, hardware unconstrained.
- Audio-coupled idea: Switch toggles with `interface/switch_001.ogg` on beat grid.
- Transition mood: Zoom slide → Scene 4 (at 14.0s).

### Scene 4 — The Payoff & Outro — 4.5s (14.0s – 18.5s)
- Center hero badge: Poco F5 Tweaker app squircle with glowing marble blueprint motif.
- Main title slams in: "Poco F5 Tweaker".
- Tagline: "Peak Hardware Tuning. Zero Root."
- Pill specs grid: "120Hz Locked" • "SkiaVK Active" • "Joyose Throttling Killed" • "Shizuku Powered".
- Final link: `github.com/ailab/f5tweaker`.
- Sequential/interaction: Specs pills pop in, final logo resolves at 17.91s (beat-locked).
- Audio intent: Grand triumphant finish.
- Audio-coupled idea: `impact/impactBell_heavy_000.ogg` at 17.91s, music outro fade.

**Music mood for this video:** Upbeat, modern electronic tech groove
**Audio summary:** Energetic beat kicks off immediately, drops tactile UI ticks on Shizuku connection and switches, culminating in a resonant payoff hit as the app locks into full power.
