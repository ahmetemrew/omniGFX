#!/usr/bin/env python3
"""Soğuk başlatmada splash karesini yakalar.

Siyah (henüz çizilmemiş) kareleri atlar; splash'i alt şerit analiziyle
doğrular: splash'te ekranın %92-97 şeridi saf marka zeminidir, ana ekranda
bu şeritte navigation bar rengi vardır. Kullanım: design_splash.py PKG OUT
"""
import io
import subprocess
import sys
import time

from PIL import Image

PKG, OUT = sys.argv[1], sys.argv[2]
# (koyu grafit, açık kâğıt) — values/colors.xml + values-night/colors.xml
BG_REFS = [(18, 26, 27), (244, 247, 246)]


def adb(*args):
    return subprocess.run(["adb", *args], capture_output=True)


def frame():
    raw = adb("exec-out", "screencap", "-p").stdout
    return Image.open(io.BytesIO(raw)).convert("RGB")


def near(px, ref, tol=6):
    return all(abs(a - b) <= tol for a, b in zip(px, ref))


adb("shell", "am", "force-stop", PKG)
adb("shell", "am", "start", "-n", f"{PKG}/.MainActivity")
for i in range(16):
    time.sleep(0.25)
    try:
        im = frame()
    except Exception:
        continue
    small = im.resize((54, 120))
    px = list(small.getdata())
    if sum(sum(p) for p in px) / (len(px) * 3) < 10:
        continue
    strip = px[111 * 54:117 * 54]
    for ref in BG_REFS:
        if sum(near(p, ref) for p in strip) / len(strip) > 0.9:
            im.save(OUT)
            print(f"SPLASH OK (frame {i}, bg {ref})")
            sys.exit(0)
print("SPLASH_MISS")
sys.exit(1)
