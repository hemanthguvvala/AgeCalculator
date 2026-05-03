#!/usr/bin/env bash
#
# build-play-screenshots.sh — batch-convert the screenshots captured during
# device testing into Play Store-ready frames with the headlines from
# PLAY_STORE_LISTING.md.
#
# Run from the repo root:
#   ./tools/build-play-screenshots.sh /tmp/zodaic-test play-store-assets
#
# Outputs eight 1080x2340 PNGs ready to drop into the Play Console listing.

set -euo pipefail

INPUT_DIR="${1:-/tmp/zodaic-test}"
OUTPUT_DIR="${2:-play-store-assets}"

mkdir -p "$OUTPUT_DIR"

FRAMER="$(cd "$(dirname "$0")" && pwd)/frame-play-screenshot.sh"
[[ -x "$FRAMER" ]] || chmod +x "$FRAMER"

# Map: input filename pattern -> headline -> output number.
# Filenames here match the captures in /tmp/zodaic-test/store-*.png.
frame() {
    local in_pattern="$1"
    local out_idx="$2"
    local headline="$3"
    local in_file
    in_file="$(ls "$INPUT_DIR"/$in_pattern 2>/dev/null | head -1 || true)"
    if [[ -z "$in_file" ]]; then
        echo "skip $out_idx: no match for $in_pattern in $INPUT_DIR" >&2
        return 0
    fi
    local out_file
    out_file="$OUTPUT_DIR/$(printf '%02d' "$out_idx")-$(basename "$in_file" .png).png"
    "$FRAMER" "$in_file" "$out_file" "$headline"
}

frame "store-01-dashboard*.png"     1 "Your daily cosmos."
frame "store-02-mood-moon*.png"     2 "How did today actually land?"
frame "store-03-profile*.png"       3 "Sun, rising, moon."
frame "store-04-trinity-chart*.png" 4 "The big three."
frame "store-05-birth-chart*.png"   5 "Where you sit in the wheel."
frame "v18-settings-premium*.png"   6 "Go ad-free. Unlock the cosmos."

echo
echo "Done. Frames in $OUTPUT_DIR/"
ls -la "$OUTPUT_DIR/"
