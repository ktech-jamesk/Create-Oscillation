"""Turn the frames captured by `./gradlew runShowcase` into GIFs plus a contact sheet.

Uses ffmpeg (palettegen/paletteuse) when it is on the PATH; falls back to Pillow otherwise.

Usage:  python tools/showcase_gifs.py [--width 600] [--fps 10] [--out media/ponder]

Reads   run/screenshots/showcase/<scene>/frame_*.png  (+ manifest.tsv)
Writes  <out>/<shot>.gif and <out>/contact_sheet.png
"""
import argparse
import glob
import os
import shutil
import subprocess
import sys

import json

from PIL import Image, ImageDraw, ImageFont

ROOT = os.path.dirname(os.path.dirname(os.path.abspath(__file__)))
SRC = os.path.join(ROOT, 'run', 'screenshots', 'showcase')


def load_manifest():
    titles = {}
    path = os.path.join(SRC, 'manifest.tsv')
    if os.path.exists(path):
        for line in open(path, encoding='utf-8'):
            parts = line.rstrip('\n').split('\t')
            if len(parts) >= 2:
                titles[parts[0]] = parts[1]
    return titles


DEFAULT_TITLE = {"anchor": "bottom-center", "width": 0.55, "margin": 0}
FONT_CANDIDATES = [r"C:\\Windows\\Fonts\\segoeui.ttf", r"C:\\Windows\\Fonts\\arial.ttf",
                   "/usr/share/fonts/truetype/dejavu/DejaVuSans.ttf", "/System/Library/Fonts/Helvetica.ttc"]


class MinecraftFont:
    """Text drawn with the game's own 8x8 glyph sheet (ascii.png), scaled, with the usual drop shadow."""

    def __init__(self, sheet, scale):
        self.sheet = sheet.convert('RGBA')
        self.scale = max(1, int(scale))
        self.size = 8 * self.scale
        self.glyphs = {}
        for code in range(256):
            gx, gy = (code % 16) * 8, (code // 16) * 8
            glyph = self.sheet.crop((gx, gy, gx + 8, gy + 8))
            bbox = glyph.getbbox()
            width = 4 if code == 32 else (bbox[2] if bbox else 0)
            self.glyphs[code] = (glyph, width)

    @staticmethod
    def find_sheet():
        home = os.path.expanduser('~')
        for root in (os.path.join(home, '.gradle', 'caches', 'neoformruntime'),):
            for dirpath, _, files in os.walk(root):
                for f in files:
                    if f.startswith('minecraft_1.21.1_client') and f.endswith('.jar'):
                        import zipfile, io
                        with zipfile.ZipFile(os.path.join(dirpath, f)) as z:
                            return Image.open(io.BytesIO(z.read('assets/minecraft/textures/font/ascii.png')))
        return None

    def textlength(self, text, font=None):
        return sum(self.glyphs[ord(c) if ord(c) < 256 else 63][1] + 1 for c in text) * self.scale

    def draw(self, overlay, xy, text, fill=(245, 245, 245, 255), shadow=(60, 60, 60, 255)):
        x, y = xy
        for c in text:
            glyph, width = self.glyphs[ord(c) if ord(c) < 256 else 63]
            if width:
                tinted = Image.new('RGBA', glyph.size, fill)
                tinted.putalpha(glyph.getchannel('A'))
                big = tinted.resize((8 * self.scale, 8 * self.scale), Image.NEAREST)
                shade = Image.new('RGBA', big.size, shadow)
                shade.putalpha(big.getchannel('A'))
                overlay.alpha_composite(shade, (int(x) + self.scale, int(y) + self.scale))
                overlay.alpha_composite(big, (int(x), int(y)))
            x += (width + 1) * self.scale


def load_font(size):
    """Prefer the game's bitmap font; fall back to a system TTF."""
    sheet = MinecraftFont.find_sheet()
    if sheet is not None:
        return MinecraftFont(sheet, round(size / 8))
    for path in FONT_CANDIDATES:
        if os.path.exists(path):
            return ImageFont.truetype(path, size)
    return ImageFont.load_default()


def text_width(draw, text, font):
    return font.textlength(text) if isinstance(font, MinecraftFont) else draw.textlength(text, font=font)


def read_spec(folder):
    path = os.path.join(folder, 'shot.json')
    if os.path.exists(path):
        return json.load(open(path, encoding='utf-8'))
    return {}


def anchor_xy(anchor, box_w, box_h, frame_w, frame_h, margin):
    anchor = (anchor or 'bottom-center').lower()
    if 'left' in anchor:
        x = margin
    elif 'right' in anchor:
        x = frame_w - box_w - margin
    else:
        x = (frame_w - box_w) // 2
    if anchor.startswith('top'):
        y = margin
    elif anchor.startswith('bottom'):
        y = frame_h - box_h - margin
    else:
        y = (frame_h - box_h) // 2
    return x, y


def load_title(path, frame_width, width_fraction):
    """The title card scaled to a fraction of the frame width (aspect kept), or None."""
    if not path or not os.path.exists(path):
        return None
    title = Image.open(path).convert('RGBA')
    target = max(1, int(frame_width * width_fraction))
    return title.resize((target, round(title.height * target / title.width)), Image.LANCZOS)


def overlay_title(im, card, spec):
    x, y = anchor_xy(spec.get('anchor'), card.width, card.height, im.width, im.height, int(spec.get('margin', 0)))
    im.alpha_composite(card, (x, y))
    return im


def wrap_text(draw, text, font, max_width):
    words, lines, line = text.split(), [], ''
    for word in words:
        trial = (line + ' ' + word).strip()
        if text_width(draw, trial, font) <= max_width or not line:
            line = trial
        else:
            lines.append(line)
            line = word
    if line:
        lines.append(line)
    return lines


def overlay_caption(im, caption, font):
    """A rounded dark box with wrapped white text at the caption's anchor (default top-right)."""
    draw = ImageDraw.Draw(im)
    max_w = int(im.width * float(caption.get('width', 0.42)))
    pad = max(6, im.width // 80)
    lines = wrap_text(draw, caption['text'], font, max_w - 2 * pad)
    line_h = font.size + (font.scale * 2 if isinstance(font, MinecraftFont) else 4)
    box_w = min(max_w, max(text_width(draw, l, font) for l in lines) + 2 * pad)
    box_h = line_h * len(lines) + 2 * pad - 4
    margin = int(caption.get('margin', im.width // 40))
    x, y = anchor_xy(caption.get('anchor', 'top-right'), int(box_w), box_h, im.width, im.height, margin)
    overlay = Image.new('RGBA', im.size, (0, 0, 0, 0))
    od = ImageDraw.Draw(overlay)
    od.rounded_rectangle((x, y, x + box_w, y + box_h), radius=pad, fill=(18, 18, 24, 200))
    ty = y + pad - 2
    for l in lines:
        if isinstance(font, MinecraftFont):
            font.draw(overlay, (x + pad, ty), l)
        else:
            od.text((x + pad, ty), l, font=font, fill=(245, 245, 245, 255))
        ty += line_h
    im.alpha_composite(overlay)
    return im


def composite_frames(folder, width, title_path, title_override, font):
    """Resize every frame and draw the shot's title + captions on it; returns the composited frames."""
    frames = sorted(glob.glob(os.path.join(folder, 'frame_*.png')))
    spec = read_spec(folder)
    interval = int(spec.get('frameInterval', 2))
    title_spec = spec.get('title', DEFAULT_TITLE)
    if title_override is not None:
        title_spec = title_override
    card = None
    if title_spec not in (False, None):
        title_spec = {**DEFAULT_TITLE, **title_spec} if isinstance(title_spec, dict) else DEFAULT_TITLE
        card = load_title(title_path, width, float(title_spec['width']))
    captions = spec.get('captions') or []
    out = []
    for i, f in enumerate(frames):
        im = Image.open(f).convert('RGBA')
        im = im.resize((width, round(im.height * width / im.width)), Image.LANCZOS)
        tick = i * interval
        for c in captions:
            if c.get('at', 0) <= tick <= c.get('until', 10 ** 9):
                overlay_caption(im, c, font)
        if card is not None:
            overlay_title(im, card, title_spec)
        out.append(im)
    return out


def ffmpeg_available():
    return shutil.which('ffmpeg') is not None


def encode_ffmpeg(images, folder, out_path, fps):
    tmp = os.path.join(folder, '_composited')
    os.makedirs(tmp, exist_ok=True)
    for old in glob.glob(os.path.join(tmp, '*.png')):
        os.remove(old)
    for i, im in enumerate(images):
        im.convert('RGB').save(os.path.join(tmp, 'frame_%05d.png' % i))
    chain = f'fps={fps},split[s0][s1];[s0]palettegen=stats_mode=diff[p];[s1][p]paletteuse=dither=sierra2_4a'
    subprocess.run(['ffmpeg', '-y', '-loglevel', 'error', '-framerate', str(fps), '-i', os.path.join(tmp, 'frame_%05d.png'),
                    '-filter_complex', chain, '-loop', '0', out_path], check=True)
    shutil.rmtree(tmp, ignore_errors=True)


def encode_pillow(images, out_path, fps):
    """Fallback when ffmpeg is missing: one shared palette so colours stay stable between frames."""
    palette_src = images[len(images) // 2].convert('RGB').quantize(colors=256, method=Image.Quantize.MEDIANCUT)
    quantised = [im.convert('RGB').quantize(palette=palette_src, dither=Image.Dither.FLOYDSTEINBERG) for im in images]
    quantised[0].save(out_path, save_all=True, append_images=quantised[1:], duration=int(1000 / fps), loop=0,
                      optimize=False, disposal=1)


def build_gif(folder, out_path, width, fps, title=None, title_override=None, font=None):
    spec = read_spec(folder)
    fps = int(spec.get('fps', fps))
    images = composite_frames(folder, width, title, title_override, font)
    if not images:
        return None
    if ffmpeg_available():
        encode_ffmpeg(images, folder, out_path, fps)
    else:
        encode_pillow(images, out_path, fps)
    return images[len(images) // 3]


def contact_sheet(stills, out_path, cols=3, cell=(400, 225), pad=12):
    if not stills:
        return
    rows = (len(stills) + cols - 1) // cols
    sheet = Image.new('RGB', (cols * cell[0] + (cols + 1) * pad, rows * (cell[1] + 24) + (rows + 1) * pad), (30, 30, 34))
    draw = ImageDraw.Draw(sheet)
    for i, (title, im) in enumerate(stills):
        r, c = divmod(i, cols)
        x = pad + c * (cell[0] + pad)
        y = pad + r * (cell[1] + 24 + pad)
        thumb = im.copy()
        thumb.thumbnail(cell, Image.LANCZOS)
        sheet.paste(thumb, (x, y), thumb)
        draw.text((x, y + cell[1] + 4), title, fill=(230, 230, 230))
    sheet.save(out_path)


def main():
    ap = argparse.ArgumentParser()
    ap.add_argument('--width', type=int, default=640)
    ap.add_argument('--fps', type=int, default=10)
    ap.add_argument('--out', default=os.path.join(ROOT, 'media', 'ponder'))
    ap.add_argument('--title', default=os.path.join(ROOT, 'title', 'title.png'), help='title card overlaid at the bottom of every frame ("" to disable)')
    ap.add_argument('--title-anchor', default=None, help='override every shot: bottom-center, bottom-left, top-right...')
    ap.add_argument('--title-width', type=float, default=None, help='override every shot: title width as a fraction of the frame width')
    ap.add_argument('--font-size', type=int, default=0, help='caption font size in px (default: width / 28)')
    ap.add_argument('--all', action='store_true', help='regenerate every captured folder, not just the shots in manifest.tsv')
    args = ap.parse_args()

    if not os.path.isdir(SRC):
        sys.exit(f'No captures at {SRC}; run ./gradlew runShowcase first')
    os.makedirs(args.out, exist_ok=True)
    titles = load_manifest()
    stills = []
    skipped = []
    for folder in sorted(glob.glob(os.path.join(SRC, '*'))):
        if not os.path.isdir(folder):
            continue
        name = os.path.basename(folder)
        # only shots captured by the last run (the manifest) regenerate; stale folders are left alone
        if titles and not args.all and name not in titles:
            skipped.append(name)
            continue
        out = os.path.join(args.out, name + '.gif')
        override = None
        if args.title_anchor or args.title_width:
            override = {**DEFAULT_TITLE}
            if args.title_anchor:
                override['anchor'] = args.title_anchor
            if args.title_width:
                override['width'] = args.title_width
        font = load_font(args.font_size or max(16, args.width // 32))
        still = build_gif(folder, out, args.width, args.fps, args.title, override, font)
        if still is None:
            continue
        size_kb = os.path.getsize(out) // 1024
        print(f'{name}.gif  {size_kb} KB')
        stills.append((titles.get(name, name), still))
    contact_sheet(stills, os.path.join(args.out, 'contact_sheet.png'))
    print(f'{len(stills)} gifs written to {args.out}')
    if skipped:
        print(f'skipped (not in manifest.tsv, use --all to include): {", ".join(skipped)}')


if __name__ == '__main__':
    main()
