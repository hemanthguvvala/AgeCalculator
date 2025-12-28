# Deep Space Glassmorphism - Quick Reference

## 🎨 Color Palette

### Primary Colors
```kotlin
BackgroundDark      #050B14   // Deep space background
PrimaryNeon         #4D96FF   // Electric blue
SurfaceGlass        White 5%  // Glass surface
BorderGlass         White 10% // Glass border
```

### Planet Colors
```kotlin
MarsRed             #FF6B6B
JupiterBeige        #E0C097
NeptuneBlue         #4D96FF
SaturnGold          #E6BE8A
VenusGold           #FFC857
EarthBlue           #4DA8DA
MercuryGray         #8D8D8D
UranusGreen         #4ECDC4
```

### Accents
```kotlin
PurpleAccent        #9B59B6
GreenAccent         #2ECC71
```

### Gradient
```kotlin
CosmicGradient      #020408 → #0B1021 (vertical)
```

## 🔤 Typography

### Display (Space Grotesk)
```kotlin
displayLarge        57sp / Bold
displayMedium       45sp / Bold
displaySmall        36sp / Bold
```

### Headlines (Space Grotesk)
```kotlin
headlineLarge       32sp / Bold
headlineMedium      28sp / Bold
headlineSmall       24sp / SemiBold
```

### Titles (Space Grotesk)
```kotlin
titleLarge          22sp / Bold
titleMedium         16sp / SemiBold
titleSmall          14sp / Medium
```

### Body (Spline Sans)
```kotlin
bodyLarge           16sp / Normal
bodyMedium          14sp / Normal
bodySmall           12sp / Normal
```

## 🧩 Component Usage

### GlassCard
```kotlin
GlassCard(
    modifier = Modifier.fillMaxWidth(),
    shape = RoundedCornerShape(24.dp)
) {
    // Your content
}
```

### StatCard
```kotlin
StatCard(
    label = "SYSTEM AGE",
    value = "25",
    modifier = Modifier.weight(1f)
)
```

### PlanetCard
```kotlin
PlanetCard(
    planetName = "MARS",
    planetAge = "13.2",
    planetColor = MarsRed,
    planetImage = { /* Icon or Image */ }
)
```

### FloatingNavBar
```kotlin
FloatingNavBar(
    items = listOf(
        NavItem(Icons.Default.Home, "Home"),
        NavItem(Icons.Default.Search, "Explore"),
        NavItem(Icons.Default.Star, "Zodiac"),
        NavItem(Icons.Default.Person, "Profile")
    ),
    selectedIndex = 0,
    onItemSelected = { index -> /* Handle */ }
)
```

### CosmicProgressBar
```kotlin
CosmicProgressBar(
    progress = 0.65f,
    brush = Brush.horizontalGradient(
        colors = listOf(PrimaryNeon, PurpleAccent)
    )
)
```

### StatsGrid
```kotlin
StatsGrid(
    items = listOf(
        StatsItem("♏", "Zodiac Sign", "Scorpio"),
        StatsItem("🌙", "Moon Phase", "Waning")
    )
)
```

## 📐 Spacing Standards

```kotlin
Extra Small         4.dp
Small               8.dp
Medium              16.dp
Large               24.dp
Extra Large         32.dp
```

## 🎯 Common Patterns

### Section Header
```kotlin
Text(
    text = "YOUR COSMIC AGE",
    style = MaterialTheme.typography.labelMedium,
    color = PrimaryNeon,
    letterSpacing = 2.sp
)
```

### Card Content Padding
```kotlin
Column(modifier = Modifier.padding(20.dp)) { }
```

### Screen Horizontal Padding
```kotlin
Column(modifier = Modifier.padding(horizontal = 16.dp)) { }
```

### Glass Border
```kotlin
.border(2.dp, PrimaryNeon, CircleShape)
```

### Secondary Text
```kotlin
color = Color.White.copy(alpha = 0.7f)
```

## 🌟 Layout Patterns

### Full Screen with Starry Background
```kotlin
Box(modifier = Modifier.fillMaxSize()) {
    StarryBackground {
        Column(
            modifier = Modifier
                .fillMaxSize()
                .background(BackgroundDark)
                .verticalScroll(rememberScrollState())
        ) {
            // Content
        }
    }
}
```

### Two-Column Stats
```kotlin
Row(
    modifier = Modifier.fillMaxWidth(),
    horizontalArrangement = Arrangement.spacedBy(12.dp)
) {
    StatCard("Label 1", "Value 1", Modifier.weight(1f))
    StatCard("Label 2", "Value 2", Modifier.weight(1f))
}
```

### Horizontal Scrolling Cards
```kotlin
Row(
    modifier = Modifier
        .fillMaxWidth()
        .horizontalScroll(rememberScrollState()),
    horizontalArrangement = Arrangement.spacedBy(16.dp)
) {
    PlanetCard(...)
    PlanetCard(...)
    PlanetCard(...)
}
```

## ⚡ Performance Tips

1. **Blur Effect**: Only works on Android 12+
2. **StarryBackground**: Uses Canvas with 100 animated stars
3. **GlassCard**: Lightweight, no performance impact
4. **ScrollState**: Remember scroll states for smooth scrolling
5. **LaunchedEffect**: Use for live age ticker updates

## 🎨 Design Tokens

### Corner Radius
- Small cards: 16.dp
- Medium cards: 20.dp
- Large cards: 24.dp
- Circular: CircleShape

### Elevation
- Glass cards: No elevation (flat design)
- Borders: 1dp (glass border)
- Avatar border: 2-3dp

### Opacity Levels
- Surface glass: 0.05f (5%)
- Border glass: 0.1f (10%)
- Secondary text: 0.7f (70%)
- Hover/Active: 0.2f (20%)

## 📱 Responsive Breakpoints

```kotlin
// Compact (Phone Portrait)
horizontal padding: 16.dp

// Medium (Tablet/Phone Landscape)
horizontal padding: 24.dp

// Expanded (Tablet Landscape)
horizontal padding: 32.dp
max content width: 800.dp
```
