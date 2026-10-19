# Hyperframes Composition Brief: Poco F5 Tweaker

## Objective
Create a short, punchy launch-style brag video for Poco F5 Tweaker (codename: `marble`).

## Output
- Composition directory: `brag-output/composition/`
- Rendered video: `brag-output/brag.mp4`
- Format: landscape — 1920x1080
- Duration: 18.5 seconds

## Source Material
- Project root: `/home/pablo/projects/aiLab/f5tweaker`
- Primary files read: `MainActivity.kt`, `TweaksViewModel.kt`, `Color.kt`, `strings.xml`
- Product name: Poco F5 Tweaker
- Tagline / strongest claim: "Hardware & Kernel Tuning (marble) — Peak Performance without Root"
- Key UI or visual moment to recreate:
  - Top app bar with green "Shizuku Active" pill
  - "Master Profile Override" card with Battery / Balanced / Performance pills
  - Grouped Inset Card with "Display Refresh Rate (120Hz)", "Disable Joyose (Caution)", "Vulkan UI Renderer (SkiaVK)", "Touch Responsiveness Boost"
- Copy that must appear verbatim:
  - "Poco F5 Tweaker"
  - "Hardware & Kernel Tuning (marble)"
  - "Shizuku Active"
  - "Bypasses MIUI thermal throttling. Active cooling strongly recommended."
  - "Peak Hardware Tuning. Zero Root."

## Creative Direction
- Tone preset: `app-store`
- Creative direction: High-performance mobile hardware launch reel
- Interpretation: Crisp Apple HIG Inset Grouped aesthetic with dark mode OLED styling; rapid, confident reveals of real hardware settings.
- Angle: Flagship Snapdragon 7+ Gen 2 locked down by MIUI thermal guardrails uncaged safely without root via Shizuku ADB.
- Hook: "Your Snapdragon 7+ Gen 2 is throttled." -> Shizuku Active.
- Outro / punchline: "Poco F5 Tweaker — Peak Hardware Tuning. Zero Root."
- Avoid:
  - Generic SaaS language
  - Abstract filler visuals
  - Unrelated visual redesign

## Visual Identity
- Background: `#000000` (Canvas deep dark), `#1C1C1E` (Apple HIG Dark Surface)
- Card Surface: `#1C1C1E` with subtle border `#38383A`
- Text: `#FFFFFF` (Primary), `#8E8E93` (Secondary / Muted)
- Accent: `#30D158` (Green / Active), `#0A84FF` (Blue), `#FF9F0A` (Orange / Performance), `#FF453A` (Red / Caution)
- Display font: `system-ui, -apple-system, BlinkMacSystemFont, "SF Pro Display", Roboto, sans-serif`
- Body font: `system-ui, -apple-system, BlinkMacSystemFont, "SF Pro Text", Roboto, sans-serif`
- Visual references from the project: Apple HIG Inset Grouped cards, rounded squircles (8dp/12dp/14dp), segmented button rows, status pills, Cupertino switches.

## Storyboard
Use the storyboard in `brag-output/brag-plan.md` as the creative contract.

Scene summary:
1. Hook & Shizuku Link — 3.7s — "Your Snapdragon 7+ Gen 2 is throttled" → Shizuku Active pill lights up
2. Master Profile Override — 5.3s (3.7s–9.0s) — Global Preset card, "Performance (120Hz • Max Boost)" tapped and active
3. Bypassing Thermal Guardrails — 5.0s (9.0s–14.0s) — "Disable Joyose" Caution switch flips, SkiaVK and Touch Boost engage
4. The Payoff & Outro — 4.5s (14.0s–18.5s) — App badge, "Poco F5 Tweaker", "Peak Hardware Tuning. Zero Root.", github link

## Audio
- Audio role: Energetic electronic tech bed with crisp tactile interface accents
- Audio arc: Begins with low anticipation tech pulse, locks in with Shizuku connection at 1.6s, accelerates through interactive toggle clicks, hits a triumphant bell at 17.9s, fades smoothly on outro.
- Music: `happy-beats-business-moves-vol-11-by-ende-dot-app.mp3`
- Music treatment: Starts at 0.0s, volume 0.35, gentle fade out during last 1.5s.
- Music cue guidance: Tempo 114.84 BPM; strong cues at 1.60s, 3.70s, 5.80s, 8.96s, 12.65s, 17.91s.
- Audio-reactive treatment: Subtle card glow / border breathing with music bass.
- Audio-coupled moments:
  - t=1.60s (beat-locked): Shizuku connected pill click (`ui/click1.ogg`)
  - t=5.80s (beat-locked): Performance profile tap (`interface/click_001.ogg`)
  - t=10.54s (beat-grid): Joyose switch flip (`interface/switch_001.ogg`)
  - t=11.60s (beat-grid): SkiaVK switch flip (`interface/switch_002.ogg`)
  - t=12.65s (beat-grid): Touch Boost switch flip (`interface/switch_001.ogg`)
  - t=17.91s (beat-locked): Final branding impact (`impact/impactBell_heavy_000.ogg`)
- SFX files: Copied into `assets/sfx/` inside composition.

## Hyperframes Instructions
Build a self-contained, valid HTML video composition in `brag-output/composition/` using standard Hyperframes conventions (`data-start`, `data-duration`, `data-track-index`, `data-volume`).
Run `npx hyperframes check` to ensure zero lint or contrast errors.
