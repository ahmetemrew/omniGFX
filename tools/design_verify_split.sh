#!/usr/bin/env bash
# Bölünmüş doğrulama — build ve emülatör AYRI flock çağrılarında.
# Neden: flock shim'i betik metninde adb görünce tüm komutu emu.lock kuyruğuna
# sokuyor; karma gate emülatör kuyruğunda açlıktan ölüyor. Build-only gradlew
# (adb yok) boş build slotunu hemen alır; emülatör oturumu ayrıca emu kuyruğuna girer.
# Bu sürücü flock ile SARILMAZ (iç flock'lar self-deadlock yaratmasın); nohup ile koşar.
set -u
cd "$(dirname "$0")/.."
L=/home/ahmete/.qwen-orch/build.lock
echo "=== SPLIT VERIFY START $(date) ==="
flock -o "$L" ./gradlew clean assembleDebug testDebugUnitTest lintDebug assembleRelease
G=$?
echo "GRADLE_RC=$G"
if [ "$G" -ne 0 ]; then
  echo "=== SPLIT VERIFY END (gradle fail) $(date) ==="
  exit "$G"
fi
flock -o "$L" bash tools/design_emulator_session.sh
S=$?
echo "SESSION_RC=$S"
flock -o "$L" bash tools/design_filled_session.sh
F=$?
echo "FILLED_SESSION_RC=$F"
echo "=== SPLIT VERIFY END $(date) ==="
exit "$F"
