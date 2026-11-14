#!/usr/bin/env bash
# Son doğrulama — TEK flock -o çağrısı içinde çalıştırılır:
#   flock -o /home/ahmete/.qwen-orch/build.lock bash tools/design_verify_final.sh
# Sıra: clean + assembleDebug + testDebugUnitTest + lintDebug + assembleRelease
#       → dolu durum emülatör oturumu (21–26 + 24b, düzeltilmiş betik).
# Ana 22 görüntü verify10'da SON APK'dan alındı; copy düzeltmeleri o ekranların
# metinlerini değiştirmez (yedek/rollback/analizör mesajları yalnız dolu akışta).
set -u
cd "$(dirname "$0")/.."
echo "=== FINAL VERIFY START $(date) ==="
./gradlew clean assembleDebug testDebugUnitTest lintDebug assembleRelease
RC=$?
echo "GRADLE_RC=$RC"
if [ "$RC" -ne 0 ]; then
  echo "=== FINAL VERIFY END (gradle fail) $(date) ==="
  exit "$RC"
fi
bash tools/design_filled_session.sh
F=$?
echo "FILLED_SESSION_RC=$F"
echo "=== FINAL VERIFY END $(date) ==="
exit "$F"