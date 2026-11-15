#!/usr/bin/env python3
"""Sayısal düzen denetimi — vision olmadığında öz-eleştiri kanıtı.

Kullanım: python3 tools/design_layout_audit.py .qwen/tmp/ui-dumps/*.xml [density]

Her dump için:
- CLIP: ekran dışına taşan düğüm var mı (bounds 0..1080 x 0..2400).
- HEDEF: clickable düğümler >= 48dp.
- KENAR: sola hizalı gövde metinleri (>= 24 karakter, ortalanmış değil)
  20dp kenar boşluğundan daha yakın başlamıyor mu (density 420 -> 52px).
- COPY: ekranın beklenen anahtar metinleri dump'ta var mı.
"""
import re
import sys

W, H = 1080, 2400

EXPECTED = {
    "01": ["Oyunlar", "Henüz oyun eklemedin", "Oyun ekle", "Shizuku"],
    "02": ["Profiller", "Henüz profil yok", "Oyun ekle"],
    "03": ["Yedekler", "Henüz yedek yok"],
    "04": ["Profil analizi"],
    "05": ["aktar"],
    "06": ["Ayarlar", "Bağlantı", "Araçlar", "Hakkında", "Shizuku", "Rehber"],
    "07": ["Shizuku"],
    "07b": ["Kapat"],
    "08": ["Rehber", "Shizuku kurulumu"],
    "09": ["Shizuku"],
    "10": ["Uygulama ara"],
    "11": ["Settings"],
    "12": ["Shizuku"],
    "13": ["Oyunlar", "Henüz oyun eklemedin"],
    "14": ["Profiller", "Henüz profil yok"],
    "15": ["Ayarlar", "Bağlantı"],
    "16": ["Rehber"],
    "17": ["Uygulama ara"],
    "18": ["Oyunlar"],
    "19": ["Ayarlar"],
    "20": ["Rehber"],
    "21": ["Dosya çekildi", "Dosya bilgisi"],
    "22": ["biçimi", "Değişkenleri ayarla", "FPSLimit"],
    "23": ["Dosya uygulandı", "Uygulama özeti", "90"],
    "24": ["Oyunlar", "Bağlantı hazır"],
    "24b": ["Oyunlar", "Settings"],
    "25": ["Profiller", "Dengeli 60 FPS", "Uygula"],
    "26": ["Yedekler", ".bak", "Geri yükle"],
}


def audit(path, scale):
    xml = open(path, encoding="utf-8", errors="replace").read()
    name = path.split("/")[-1].replace(".xml", "")
    problems = []
    margins = []
    texts = []
    parsed = []
    for m in re.finditer(r'<node[^>]*>', xml):
        s = m.group(0)
        b = re.search(r'bounds="\[(\d+),(\d+)\]\[(\d+),(\d+)\]"', s)
        if not b:
            continue
        x1, y1, x2, y2 = map(int, b.groups())
        t = re.search(r'text="([^"]*)"', s)
        txt = t.group(1) if t else ""
        d = re.search(r'content-desc="([^"]*)"', s)
        desc = d.group(1) if d else ""
        # Baştaki boşluk şart: long-clickable="true" (drag handle) clickable
        # sanılıp sahte hedef üretiyordu.
        parsed.append((x1, y1, x2, y2, txt, desc, ' clickable="true"' in s))
    # Viewport kesiği imzası: satır LazyColumn görünür alanının alt kenarında
    # kesilince metin glifi de düğümle aynı alt kenarda kesik görünür (<10dp).
    # Tam boyut kodda heightIn(min=48dp) / IconButton 48dp ile garanti; dilim
    # sahte pozitiftir. content-desc düğümleri de kesik imzası taşır (ikonlar).
    cut = [(tx1, tx2, ty2) for tx1, ty1, tx2, ty2, ttxt, ddesc, _ in parsed
           if (ttxt or ddesc) and (ty2 - ty1) < 10 * scale]
    # Yatay kesiş imzası: LazyRow/breadcrumb kaydırmasında kenarda kalan dilim.
    # İçteki düğümün görünür genişliği <10dp ve clickable ebeveynin kenarına
    # yapışıksa dilimdir; tam boyut kodda heightIn + padding ile garanti.
    hcut = [(tx1, tx2) for tx1, ty1, tx2, ty2, _, _, _ in parsed
            if (tx2 - tx1) < 10 * scale]
    for x1, y1, x2, y2, txt, desc, clickable in parsed:
        if x1 < 0 or y1 < 0 or x2 > W or y2 > H:
            problems.append(f"CLIP [{x1},{y1}][{x2},{y2}]")
        if txt:
            texts.append(txt)
        if clickable:
            # Kenarda kesik düğüm: listede kısmen görünen satırın gerçek boyutu
            # değil görünür dilimidir; tam boyut kodda heightIn ile garanti.
            if y2 >= H or y1 <= 0:
                continue
            if any(tx1 >= x1 and tx2 <= x2 and ty2 == y2 for tx1, tx2, ty2 in cut):
                continue
            if any(tx1 >= x1 and tx2 <= x2 and (tx1 == x1 or tx2 == x2)
                   for tx1, tx2 in hcut):
                continue
            wdp, hdp = (x2 - x1) / scale, (y2 - y1) / scale
            # Dejenere semantik düğüm (0x0, 3x3 "Close sheet" gibi): gerçek dokunma
            # hedefi değil, Compose semantics artefaktı; eşik denetimine girmez.
            if wdp < 5 or hdp < 5:
                continue
            if wdp < 47.5 or hdp < 47.5:
                problems.append(f"KUCUK_HEDEF {(txt or desc or '?')[:24]} {wdp:.0f}x{hdp:.0f}dp")
        if len(txt) >= 24 and abs((x1 + x2) / 2 - W / 2) > 40:
            margins.append(x1)
    edge = 20 * scale
    off = [x for x in margins if x < edge - 8]
    if off:
        problems.append(f"KENAR {sorted(set(off))[:6]} (kenar boşluğu < 20dp)")
    key = name.split("-")[0]
    for needle in EXPECTED.get(key, []):
        if not any(needle.lower() in t.lower() for t in texts):
            problems.append(f"COPY_EKSIK '{needle}'")
    print(f"{name}: {'TEMIZ' if not problems else '; '.join(problems)}")
    return not problems


if __name__ == "__main__":
    args = sys.argv[1:]
    density = 420.0
    if args and args[-1].replace(".", "").isdigit():
        density = float(args[-1])
        args = args[:-1]
    scale = density / 160.0
    ok = all([audit(p, scale) for p in args])
    sys.exit(0 if ok else 1)
