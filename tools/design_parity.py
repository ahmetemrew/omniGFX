#!/usr/bin/env python3
"""before/after çift denkliği — tema tarafı + parlaklık taraması.

Kullanım: python3 tools/design_parity.py [before_dir] [after_dir]

Her before/NN-*.png için after/NN-*.png çiftini bulur:
- TEMA: köşe pikselleri (4 köşe, 3px içeriden) koyu/açık zeminle eşleşiyor mu;
  iki taraf aynı tema tarafında olmalı (SAME_SIDE).
- PARLAKLIK: ortalama gri oranı; >2.0 oran farklı durum/tema şüphesi doğurur
  (içerik değişimi meşru olabilir, elle incelenir).
Dosya adı = ekran-durum; eşleşmeyen çiftler RAPOR edilir.
"""
import os
import sys

from PIL import Image

def theme_side(im):
    # before eski kimliğin paletini taşır; tema tarafı paletten bağımsız
    # köşe parlaklığıyla sınıflandırılır (koyu < 60, açık > 180).
    w, h = im.size
    corners = [im.getpixel((x, y)) for x, y in
               ((3, 3), (w - 4, 3), (3, h - 4), (w - 4, h - 4))]
    lum = [0.299 * r + 0.587 * g + 0.114 * b for r, g, b in corners]
    dark = sum(v < 60 for v in lum)
    light = sum(v > 180 for v in lum)
    if dark >= 3:
        return "koyu"
    if light >= 3:
        return "acik"
    return "belirsiz"


def brightness(im):
    g = im.convert("L").resize((54, 120))
    data = g.tobytes()
    return sum(data) / len(data)


def main():
    before_dir = sys.argv[1] if len(sys.argv) > 1 else "design/before"
    after_dir = sys.argv[2] if len(sys.argv) > 2 else "design/after"
    ok = True
    pairs = 0
    for name in sorted(os.listdir(before_dir)):
        if not name.endswith(".png"):
            continue
        bpath = os.path.join(before_dir, name)
        apath = os.path.join(after_dir, name)
        if not os.path.exists(apath):
            print(f"{name}: AFTER_YOK")
            ok = False
            continue
        pairs += 1
        bim = Image.open(bpath).convert("RGB")
        aim = Image.open(apath).convert("RGB")
        bt, at = theme_side(bim), theme_side(aim)
        bb, ab = brightness(bim), brightness(aim)
        ratio = max(bb, ab) / max(1.0, min(bb, ab))
        same = bt == at and bt != "belirsiz"
        flag = "SAME_SIDE" if same else "TEMA_FARKI"
        if not same:
            ok = False
        if ratio > 2.0:
            flag += f" PARLAKLIK_ORANI={ratio:.2f}"
        print(f"{name}: {flag} before={bt} after={at} "
              f"parlaklik={bb:.0f}/{ab:.0f}")
    print(f"PARITY_PAIRS={pairs} PARITY_OK={int(ok)}")
    sys.exit(0 if ok else 1)


if __name__ == "__main__":
    main()
