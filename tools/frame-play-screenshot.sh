#!/usr/bin/env bash
#
# frame-play-screenshot.sh — convert a raw device screenshot into a
# Play Store-ready 1080x2340 PNG with an optional headline overlay.
#
# Usage:
#   tools/frame-play-screenshot.sh <input.png> <output.png> [headline]
#
# Examples:
#   ./frame-play-screenshot.sh raw.png out.png
#   ./frame-play-screenshot.sh raw.png out.png "A new reading. Every day."
#
# Output spec (matches Play Store guidance):
#   1080x2340, 16:9 portrait-ish, header strip with sign-palette gradient.

set -euo pipefail

INPUT="${1:?Usage: $0 <input.png> <output.png> [headline]}"
OUTPUT="${2:?Usage: $0 <input.png> <output.png> [headline]}"
HEADLINE="${3:-}"

[[ -f "$INPUT" ]] || { echo "Input not found: $INPUT" >&2; exit 1; }

WIDTH=1080
HEIGHT=2340
HEADER_HEIGHT=320
INNER_HEIGHT=$(( HEIGHT - HEADER_HEIGHT ))

# Cosmic gradient: deep navy -> aurora purple. Matches in-app palette.
GRADIENT_TOP="#1F1A38"
GRADIENT_BOT="#0A0F1F"

# 1) Resize the raw screenshot to fill the lower 2020px of frame, preserving
#    aspect with a centered crop.
TMP_BODY=$(mktemp --suffix=.png)
TMP_HEADER=$(mktemp --suffix=.png)
trap 'rm -f "$TMP_BODY" "$TMP_HEADER"' EXIT

convert "$INPUT" \
    -resize "${WIDTH}x${INNER_HEIGHT}^" \
    -gravity center \
    -extent "${WIDTH}x${INNER_HEIGHT}" \
    "$TMP_BODY"

# 2) Header strip with vertical gradient + faint star dots.
convert -size "${WIDTH}x${HEADER_HEIGHT}" \
    gradient:"${GRADIENT_TOP}-${GRADIENT_BOT}" \
    \( -size "${WIDTH}x${HEADER_HEIGHT}" xc:none \
       -fill "rgba(255,224,151,0.35)" -draw "circle 200,90 200,93" \
       -fill "rgba(255,255,255,0.45)" -draw "circle 880,180 880,182" \
       -fill "rgba(143,182,255,0.40)" -draw "circle 540,240 540,243" \) \
    -compose over -composite \
    "$TMP_HEADER"

# 3) If headline provided, draw it centered in the header strip.
if [[ -n "$HEADLINE" ]]; then
    # Two-pass: subtle gold line above, white headline below.
    convert "$TMP_HEADER" \
        -font "DejaVu-Sans-Bold" \
        -pointsize 32 \
        -fill "#E0C097" \
        -gravity north \
        -annotate +0+90 "ZODAIC" \
        -font "DejaVu-Sans-Bold" \
        -pointsize 64 \
        -fill "#FFFFFF" \
        -gravity north \
        -annotate +0+150 "$HEADLINE" \
        "$TMP_HEADER"
fi

# 4) Stack: header on top, body below. Final output.
convert "$TMP_HEADER" "$TMP_BODY" -append "$OUTPUT"

echo "Wrote $OUTPUT (${WIDTH}x${HEIGHT})"
