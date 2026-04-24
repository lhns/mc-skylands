#!/usr/bin/env python3
"""Generate the 3x3x3 empty-arena structure NBT used by GameTests.

Runs on Python 3 stdlib only. Output is gzipped NBT ready to drop into
``common/src/main/resources/data/skylands/structure/empty.nbt``.

Layout matches what Minecraft 1.21.1 expects for a structure file referenced
from ``@GameTest(template = "skylands:empty")``:

* root: unnamed Compound
  * DataVersion: Int = 3953 (1.21.1)
  * size:        List[Int]  = [3, 3, 3]
  * entities:    List[Compound] = []
  * blocks:      List[Compound] = []
  * palette:     List[Compound] = [{Name: "minecraft:air"}]

Usage::

    python scripts/gen_empty_structure.py > common/src/main/resources/data/skylands/structure/empty.nbt
"""

import gzip
import struct
import sys

TAG_END = 0
TAG_INT = 3
TAG_STRING = 8
TAG_LIST = 9
TAG_COMPOUND = 10

DATA_VERSION = 3953  # Minecraft 1.21.1


def build() -> bytes:
    out = bytearray()
    # Outer compound, name="" (one header for the whole payload).
    out += bytes([TAG_COMPOUND, 0x00, 0x00])

    # DataVersion: Int
    out += bytes([TAG_INT, 0x00, len("DataVersion")]) + b"DataVersion"
    out += struct.pack(">i", DATA_VERSION)

    # size: List[Int] = [3, 3, 3]
    out += bytes([TAG_LIST, 0x00, len("size")]) + b"size"
    out += bytes([TAG_INT]) + struct.pack(">i", 3)
    out += struct.pack(">iii", 3, 3, 3)

    # entities: List[Compound] = []
    out += bytes([TAG_LIST, 0x00, len("entities")]) + b"entities"
    out += bytes([TAG_COMPOUND]) + struct.pack(">i", 0)

    # blocks: List[Compound] = []
    out += bytes([TAG_LIST, 0x00, len("blocks")]) + b"blocks"
    out += bytes([TAG_COMPOUND]) + struct.pack(">i", 0)

    # palette: List[Compound] = [{Name: "minecraft:air"}]
    out += bytes([TAG_LIST, 0x00, len("palette")]) + b"palette"
    out += bytes([TAG_COMPOUND]) + struct.pack(">i", 1)
    # palette[0] body (no outer Compound tag/name, just body + TAG_END)
    out += bytes([TAG_STRING, 0x00, len("Name")]) + b"Name"
    out += struct.pack(">H", len("minecraft:air")) + b"minecraft:air"
    out += bytes([TAG_END])  # end palette[0]

    out += bytes([TAG_END])  # end outer compound
    return bytes(out)


def main() -> None:
    sys.stdout.buffer.write(gzip.compress(build()))


if __name__ == "__main__":
    main()
