"""Generates Copperpot's launcher and splash icons from one geometry definition.

    python3 tools/icon/generate_icon.py

Writes the Android vector drawables (adaptive launcher icon, themed monochrome layer and splash
screen) and tools/icon/copperpot-icon.svg, the source for the 512px store icons. Coordinates use a
108x108 viewport; everything stays inside the 66dp circle that launchers never crop.
"""
import pathlib

ROOT = pathlib.Path(__file__).resolve().parents[2]
RES = ROOT / "app/src/main/res"

BACKGROUND = "#FE8A00"  # primaryContainer of tools/theme/generate_color_scheme.py
POT = "#FFF4E8"

BODY = "M31,58.5H77V66C77,74.3 70.3,81 62,81H46C37.7,81 31,74.3 31,66Z"
LID = ("M28.5,50.5C28.5,48.3 30.3,46.5 32.5,46.5H75.5C77.7,46.5 79.5,48.3 79.5,50.5V54.5"
       "C79.5,55.6 78.6,56.5 77.5,56.5H30.5C29.4,56.5 28.5,55.6 28.5,54.5Z")
KNOB = "M50.5,40.5C50.5,39.4 51.4,38.5 52.5,38.5H55.5C56.6,38.5 57.5,39.4 57.5,40.5V46.5H50.5Z"
HANDLE_LEFT = "M23.5,60.5C23.5,59.4 24.4,58.5 25.5,58.5H31V63.5H25.5C24.4,63.5 23.5,62.6 23.5,61.5Z"
HANDLE_RIGHT = "M84.5,60.5C84.5,59.4 83.6,58.5 82.5,58.5H77V63.5H82.5C83.6,63.5 84.5,62.6 84.5,61.5Z"
FILLS = [BODY, LID, KNOB, HANDLE_LEFT, HANDLE_RIGHT]
STEAM = ["M44,35C41,32 47,29.5 44,26.5", "M54,33C51,30 57,27.5 54,24.5", "M64,35C61,32 67,29.5 64,26.5"]
STEAM_WIDTH = "2.6"


def vector(size_dp: int, body: str) -> str:
    return (
        '<vector xmlns:android="http://schemas.android.com/apk/res/android"\n'
        f'\tandroid:width="{size_dp}dp"\n'
        f'\tandroid:height="{size_dp}dp"\n'
        '\tandroid:viewportWidth="108"\n'
        '\tandroid:viewportHeight="108">\n'
        f"{body}</vector>\n"
    )


def fill_path(data: str, color: str) -> str:
    return f'\t<path\n\t\tandroid:fillColor="{color}"\n\t\tandroid:pathData="{data}" />\n'


def steam_path(data: str, color: str) -> str:
    return (
        f'\t<path\n\t\tandroid:pathData="{data}"\n\t\tandroid:strokeColor="{color}"\n'
        f'\t\tandroid:strokeWidth="{STEAM_WIDTH}"\n\t\tandroid:strokeLineCap="round"\n'
        '\t\tandroid:strokeLineJoin="round" />\n'
    )


def pot(color: str) -> str:
    return "".join(fill_path(d, color) for d in FILLS) + "".join(steam_path(d, color) for d in STEAM)


def write(path: pathlib.Path, text: str) -> None:
    path.parent.mkdir(parents=True, exist_ok=True)
    path.write_text(text)


def main() -> None:
    background = fill_path("M0,0h108v108h-108z", BACKGROUND)
    # Launcher: 108dp layers
    write(RES / "drawable/ic_launcher_background.xml", vector(108, background))
    write(RES / "drawable/ic_launcher_foreground.xml", vector(108, pot(POT)))
    # Themed icon (Android 13+): the system tints the alpha channel, so any opaque colour works
    write(RES / "drawable/ic_launcher_monochrome.xml", vector(108, pot("#FFFFFFFF")))
    # Splash screen: the same artwork at 288dp, one set for light and dark
    write(RES / "drawable/ic_splash_screen_background.xml", vector(288, background))
    write(RES / "drawable/ic_splash_screen_foreground.xml", vector(288, pot(POT)))

    svg_pot = "".join(f'<path d="{d}" fill="{POT}"/>' for d in FILLS) + "".join(
        f'<path d="{d}" fill="none" stroke="{POT}" stroke-width="{STEAM_WIDTH}" '
        'stroke-linecap="round" stroke-linejoin="round"/>' for d in STEAM
    )
    write(
        ROOT / "tools/icon/copperpot-icon.svg",
        f'<svg xmlns="http://www.w3.org/2000/svg" viewBox="0 0 108 108" width="512" height="512">'
        f'<rect width="108" height="108" fill="{BACKGROUND}"/>{svg_pot}</svg>\n',
    )
    print("icon resources written")


if __name__ == "__main__":
    main()
