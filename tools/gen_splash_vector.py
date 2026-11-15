#!/usr/bin/env python3
"""Splash marka vektörünü üretir: kalibrasyon kadranı + gerçek font glifleriyle wordmark.

Barlow SemiBold ("OmniGFX") ve JetBrains Mono ("CALIBRATION SUITE") glif dış hatları
fontTools ile Android vector path verisine çevrilir — elle çizilmiş taklit harf yok.
Çıktı: app/src/main/res/drawable/ic_splash_brand.xml (açık) + drawable-night/ (koyu).
Kullanım: python3 tools/gen_splash_vector.py
"""
import math
import os

from fontTools.pens.basePen import BasePen
from fontTools.ttLib import TTFont

ROOT = os.path.dirname(os.path.dirname(os.path.abspath(__file__)))
FONT_DIR = os.path.join(ROOT, "core", "core-ui", "src", "main", "res", "font")
OUT_LIGHT = os.path.join(ROOT, "app", "src", "main", "res", "drawable", "ic_splash_brand.xml")
OUT_DARK = os.path.join(ROOT, "app", "src", "main", "res", "drawable-night", "ic_splash_brand.xml")

VIEW = 240.0
CX = 120.0
EMBLEM_CY = 90.0
RING_R = 36.0


class AndroidPen(BasePen):
    """Glif dış hattını Android pathData komutlarına çevirir (y ekseni ters)."""

    def __init__(self, glyph_set, scale, ox, oy):
        super().__init__(glyph_set)
        self.scale = scale
        self.ox = ox
        self.oy = oy
        self.parts = []

    def pt(self, p):
        x, y = p
        return f"{self.ox + x * self.scale:.2f},{self.oy - y * self.scale:.2f}"

    def _moveTo(self, p):
        self.parts.append(f"M{self.pt(p)}")

    def _lineTo(self, p):
        self.parts.append(f"L{self.pt(p)}")

    def _curveToOne(self, p1, p2, p3):
        self.parts.append(f"C{self.pt(p1)} {self.pt(p2)} {self.pt(p3)}")

    def _qCurveToOne(self, p1, p2):
        self.parts.append(f"Q{self.pt(p1)} {self.pt(p2)}")

    def _closePath(self):
        self.parts.append("Z")

    def path(self):
        return "".join(self.parts)


def text_path(font_path, text, size, baseline_y, tracking=0.0):
    font = TTFont(font_path)
    upem = font["head"].unitsPerEm
    scale = size / upem
    cmap = font.getBestCmap()
    glyph_set = font.getGlyphSet()
    hmtx = font["hmtx"]

    advances = []
    for ch in text:
        gname = cmap[ord(ch)]
        advances.append(hmtx[gname][0] * scale + tracking)
    total = sum(advances) - (tracking if text else 0.0)
    ox = CX - total / 2.0

    paths = []
    x = ox
    for ch, adv in zip(text, advances):
        gname = cmap[ord(ch)]
        if ch != " ":
            pen = AndroidPen(glyph_set, scale, x, baseline_y)
            glyph_set[gname].draw(pen)
            d = pen.path()
            if d:
                paths.append(d)
        x += adv
    return " ".join(paths)


def emblem_paths(petrol, brass):
    out = []
    # Halka
    out.append(
        f'<path android:pathData="M{CX - RING_R},{EMBLEM_CY} '
        f"A{RING_R},{RING_R} 0 1 1 {CX + RING_R},{EMBLEM_CY} "
        f'A{RING_R},{RING_R} 0 1 1 {CX - RING_R},{EMBLEM_CY} Z" '
        f'android:strokeColor="{petrol}" android:strokeWidth="3" '
        f'android:fillColor="#00000000" />'
    )
    # Tikler: 24 göz, çiftler ana tik
    for i in range(24):
        ang = math.radians(i * 15.0 - 90.0)
        major = i % 2 == 0
        r1, r2 = (30.0, 25.0) if major else (30.0, 27.5)
        x1, y1 = CX + r1 * math.cos(ang), EMBLEM_CY + r1 * math.sin(ang)
        x2, y2 = CX + r2 * math.cos(ang), EMBLEM_CY + r2 * math.sin(ang)
        alpha = "0.85" if major else "0.4"
        width = "2" if major else "1.2"
        out.append(
            f'<path android:pathData="M{x1:.2f},{y1:.2f} L{x2:.2f},{y2:.2f}" '
            f'android:strokeColor="{petrol}" android:strokeAlpha="{alpha}" '
            f'android:strokeWidth="{width}" android:strokeLineCap="round" '
            f'android:fillColor="#00000000" />'
        )
    # İbre: sağ üste bakan pirinç gösterge + kısa kuyruk
    ang = math.radians(-35.0)
    tipx, tipy = CX + 26 * math.cos(ang), EMBLEM_CY + 26 * math.sin(ang)
    tailx, taily = CX - 8 * math.cos(ang), EMBLEM_CY - 8 * math.sin(ang)
    out.append(
        f'<path android:pathData="M{tailx:.2f},{taily:.2f} L{tipx:.2f},{tipy:.2f}" '
        f'android:strokeColor="{brass}" android:strokeWidth="3.5" '
        f'android:strokeLineCap="round" android:fillColor="#00000000" />'
    )
    # Göbek
    out.append(
        f'<path android:pathData="M{CX - 4.5},{EMBLEM_CY} '
        f"A4.5,4.5 0 1 1 {CX + 4.5},{EMBLEM_CY} "
        f'A4.5,4.5 0 1 1 {CX - 4.5},{EMBLEM_CY} Z" '
        f'android:fillColor="{brass}" />'
    )
    return out


def build(petrol, brass, wordmark, micro):
    barlow = os.path.join(FONT_DIR, "barlow_semibold.ttf")
    mono = os.path.join(FONT_DIR, "jetbrains_mono_regular.ttf")
    word = text_path(barlow, "OmniGFX", 22.0, 160.0)
    tag = text_path(mono, "CALIBRATION SUITE", 11.0, 181.0, tracking=1.2)

    lines = [
        '<?xml version="1.0" encoding="utf-8"?>',
        "<!--",
        "  Açılış ekranı marka kilidi: kalibrasyon kadranı (72dp halka + tik rayı +",
        "  pirinç ibre) altında Barlow SemiBold wordmark ve JetBrains Mono etiket.",
        "  Glif yolları tools/gen_splash_vector.py ile gerçek fontlardan üretildi.",
        "  Üstteki BÜYÜK HARFLİ etiket bölüm başlığı değil, marka kilidinin parçası.",
        "  VectorRaster/VectorPath uyarıları bilinçli susturuldu: bu bir ikon değil,",
        "  tek seferlik çizilen açılış kilidi; 240dp ve uzun glif yolları gerekli.",
        "-->",
        '<vector xmlns:android="http://schemas.android.com/apk/res/android"',
        '    xmlns:tools="http://schemas.android.com/tools"',
        '    android:width="240dp"',
        '    android:height="240dp"',
        '    android:viewportWidth="240"',
        '    android:viewportHeight="240"',
        '    tools:ignore="VectorRaster,VectorPath">',
    ]
    lines += ["    " + p for p in emblem_paths(petrol, brass)]
    lines.append(f'    <path android:pathData="{word}" android:fillColor="{wordmark}" />')
    lines.append(f'    <path android:pathData="{tag}" android:fillColor="{micro}" />')
    lines.append("</vector>")
    return "\n".join(lines) + "\n"


def main():
    light = build("#0E5F56", "#8A5C10", "#16211F", "#4C5A58")
    dark = build("#6ECDBD", "#E3A94F", "#E4EAE9", "#A8B6B5")
    for path, content in ((OUT_LIGHT, light), (OUT_DARK, dark)):
        os.makedirs(os.path.dirname(path), exist_ok=True)
        with open(path, "w", encoding="utf-8") as f:
            f.write(content)
        print(f"WROTE {path} ({len(content)} bytes)")


if __name__ == "__main__":
    main()
