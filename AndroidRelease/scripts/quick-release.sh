#!/usr/bin/env bash
set -euo pipefail

profile="${1:-}"
if [[ -z "$profile" ]]; then
  echo "Usage: $0 <profile>" >&2
  echo "Example: $0 duoplay" >&2
  exit 2
fi

root="$(cd "$(dirname "$0")/../.." && pwd)"
exec "$root/wrapper" build \
  --profile "$profile" \
  --platform android \
  --configuration release \
  --artifact aab
