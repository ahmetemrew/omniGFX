#!/usr/bin/env bash
# Tam doğrulama + dolu durumlar — TEK flock -o çağrısı içinde çalıştırılır:
#   flock -o /home/ahmete/.qwen-orch/build.lock bash tools/design_verify_full.sh
# Sıra: clean build + test + lint + release → ana emulator/adb görüntü oturumu
#       → dolu durum emulator oturumu (Shizuku ADB modu). İki oturum sıralıdır.
set -u
cd "$(dirname "$0")/.."
echo "=== FULL VERIFY START $(date) ==="
bash tools/design_verify_all.sh
V=$?
echo "VERIFY_ALL_RC=$V"
if [ "$V" -ne 0 ]; then
  echo "=== FULL VERIFY END (verify fail) $(date) ==="
  exit "$V"
fi
bash tools/design_filled_session.sh
F=$?
echo "FILLED_SESSION_RC=$F"
echo "=== FULL VERIFY END $(date) ==="
exit "$F"
