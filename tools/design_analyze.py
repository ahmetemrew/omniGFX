#!/usr/bin/env python3
"""Tasarım kanıt analizi — görüntü paleti + UI dump dokunma hedefleri.

Kullanım:
  python3 tools/design_analyze.py palette design/after/*.png
  python3 tools/design_analyze.py targets .qwen/tmp/ui-dumps/*.xml [density]

palette: baskın renkleri DESIGN.md paletiyle karşılaştırır; palet dışı ve
iki palet renginin karışımı da olmayan, >%1.5 kapsamlı renkleri işaretler.
targets: clickable düğümlerin dp boyutlarını ölçer, <48dp olanları işaretler.
"""
import re
import sys
from collections import Counter

DARK = {
    "bg": (18, 26, 27), "surface": (22, 31, 32), "scLow": (26, 36, 37),
    "sc": (31, 42, 43), "scHigh": (37, 49, 50), "scHighest": (44, 57, 58),
    "onSurface": (228, 234, 233), "onSurfaceVar": (168, 182, 181),
    "primary": (110, 205, 189), "onPrimary": (6, 35, 31),
    "primaryC": (20, 69, 62), "onPrimaryC": (168, 230, 218),
    "secondary": (227, 169, 79), "onSecondary": (42, 27, 2),
    "secondaryC": (74, 52, 19), "onSecondaryC": (246, 217, 164),
    "error": (242, 131, 122), "onError": (58, 10, 6),
    "errorC": (92, 31, 25), "onErrorC": (251, 213, 208),
    "outline": (100, 118, 115), "outlineVar": (46, 59, 58),
}
LIGHT = {
    "bg": (244, 247, 246), "surface": (255, 255, 255), "scLow": (240, 244, 243),
    "sc": (234, 239, 238), "scHigh": (227, 233, 232), "scHighest": (219, 227, 226),
    "onSurface": (22, 33, 31), "onSurfaceVar": (76, 90, 88),
    "primary": (14, 95, 86), "onPrimary": (255, 255, 255),
    "primaryC": (205, 234, 228), "onPrimaryC": (6, 48, 42),
    "secondary": (138, 92, 16), "onSecondary": (255, 255, 255),
    "secondaryC": (247, 227, 190), "onSecondaryC": (62, 42, 5),
    "error": (168, 52, 42), "onError": (255, 255, 255),
    "errorC": (250, 220, 216), "onErrorC": (74, 15, 9),
    "outline": (124, 139, 137), "outlineVar": (198, 208, 207),
}


def dist(a, b):
    return max(abs(x - y) for x, y in zip(a, b))


def on_segment(c, a, b):
    """c, a-b doğru parçasının yakınında mı (alpha karışımı)?"""
    ab = [y - x for x, y in zip(a, b)]
    denom = sum(v * v for v in ab)
    if denom == 0:
        return dist(c, a) < 12
    t = sum((c[i] - a[i]) * ab[i] for i in range(3)) / denom
    if not 0 <= t <= 1:
        return False
    proj = [a[i] + t * ab[i] for i in range(3)]
    return dist(c, proj) < 12


def palette_mode(paths):
    from PIL import Image
    for path in paths:
        im = Image.open(path).convert("RGB").resize((108, 240))
        px = list(im.getdata())
        cnt = Counter(px)
        total = len(px)
        dom, domn = cnt.most_common(1)[0]
        theme = "koyu" if dist(dom, DARK["bg"]) < dist(dom, LIGHT["bg"]) else "acik"
        pal = DARK if theme == "koyu" else LIGHT
        entries = list(pal.values())
        flagged = []
        for color, n in cnt.most_common(40):
            frac = n / total
            if frac < 0.015:
                continue
            if any(dist(color, e) <= 14 for e in entries):
                continue
            if any(on_segment(color, a, b) for i, a in enumerate(entries) for b in entries[i + 1:]):
                continue
            flagged.append((color, round(frac * 100, 1)))
        top = ", ".join(f"#{c[0]:02X}{c[1]:02X}{c[2]:02X} %{round(n / total * 100, 1)}"
                        for c, n in cnt.most_common(4))
        status = "TEMIZ" if not flagged else f"PALET_DISI {flagged}"
        print(f"{path.split('/')[-1]} [{theme}] {top} -> {status}")


def targets_mode(paths, density):
    scale = density / 160.0
    for path in paths:
        xml = open(path, encoding="utf-8", errors="replace").read()
        small = []
        for m in re.finditer(r'<node[^>]*>', xml):
            s = m.group(0)
            if 'clickable="true"' not in s:
                continue
            b = re.search(r'bounds="\[(\d+),(\d+)\]\[(\d+),(\d+)\]"', s)
            if not b:
                continue
            x1, y1, x2, y2 = map(int, b.groups())
            wdp, hdp = (x2 - x1) / scale, (y2 - y1) / scale
            if wdp < 47.5 or hdp < 47.5:
                t = re.search(r'text="([^"]*)"', s)
                d = re.search(r'content-desc="([^"]*)"', s)
                name = (t.group(1) or d.group(1) or "?")[:30]
                small.append(f"{name} {wdp:.0f}x{hdp:.0f}dp")
        name = path.split("/")[-1]
        print(f"{name}: {'TEMIZ' if not small else 'KUCUK_HEDEF ' + '; '.join(small)}")


if __name__ == "__main__":
    mode = sys.argv[1]
    if mode == "palette":
        palette_mode(sys.argv[2:])
    else:
        density = float(sys.argv[-1]) if sys.argv[-1].replace(".", "").isdigit() else 420.0
        files = sys.argv[2:-1] if sys.argv[-1].replace(".", "").isdigit() else sys.argv[2:]
        targets_mode(files, density)
