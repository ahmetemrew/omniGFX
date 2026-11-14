#!/usr/bin/env bash
# Tam doğrulama — TEK flock -o çağrısı içinde çalıştırılır:
#   flock -o /home/ahmete/.qwen-orch/build.lock bash tools/design_verify_all.sh
# Sıra: clean + assembleDebug + testDebugUnitTest + lintDebug + assembleRelease (CI eşleniği)
#       → emülatör oturumu (kur, görüntüler, çökme taraması, kapat).
set -u
cd "$(dirname "$0")/.."
echo "=== VERIFY START $(date) ==="
./gradlew clean assembleDebug testDebugUnitTest lintDebug assembleRelease
RC=$?
echo "GRADLE_RC=$RC"
if [ "$RC" -ne 0 ]; then
  echo "=== VERIFY END (gradle fail) $(date) ==="
  exit "$RC"
fi
bash tools/design_emulator_session.sh
SRC=$?
echo "SESSION_RC=$SRC"
echo "=== VERIFY END $(date) ==="
exit "$SRC"
