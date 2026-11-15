#!/usr/bin/env bash
# verify17 — yalnızca emülatör oturumları (build gate ayrı koştu: 03:33 BUILD
# SUCCESSFUL, 76 test 0 fail, lint 0 hata; bkz. .qwen/tmp/build17.log).
# Sürücü flock ile SARILMAZ; her oturum kendi flock -o çağrısında emu kuyruğuna
# girer (shim, betik gövdesinde adb görünce emu.lock'a yönlendirir).
set -u
cd "$(dirname "$0")/.."
L=/home/ahmete/.qwen-orch/build.lock
echo "=== VERIFY17 SESSIONS START $(date) ==="
flock -o "$L" bash tools/design_emulator_session.sh
S=$?
echo "SESSION_RC=$S"
flock -o "$L" bash tools/design_filled_session.sh
F=$?
echo "FILLED_SESSION_RC=$F"
echo "=== VERIFY17 SESSIONS END $(date) ==="
[ "$S" -eq 0 ] && [ "$F" -eq 0 ]