# Getting Started with Celestials

Celestials are the stars, planets, moons, and other astronomical bodies in Genesis. They define positions, orbits, and physical properties of every object in your universe.

## Core Concepts

### What is a Celestial?

Each celestial has:

- **Type** — What kind it is (`neogenesis:star`, `neogenesis:body`, `neogenesis:blackhole`, or custom)
- **Physical properties** — Size, gravity, color, and type-specific properties
- **Transform** — How it moves and rotates over time

The celestial's ID is derived from its data file path: `data/<namespace>/genesis/celestials/<name>.json` → `namespace:name`. If a celestial has an associated dimension, use the same ID.

### Celestial Types

| Type | Light | Shadow | Visitable | Use for |
|------|-------|--------|-----------|---------|
| `neogenesis:star` | yes | no | no | Suns |
| `neogenesis:body` | no | yes | yes | Planets, moons |
| `neogenesis:blackhole` | no | no | no | Black holes |

Addon mods can register additional types — see [Advanced Topics](advanced.md).

### Transform Providers

| Type | Use for |
|------|---------|
| `neogenesis:static` | Fixed position (e.g. a central sun) |
| `neogenesis:orbiting` | Circular orbit around a parent celestial |

### Properties

Each celestial type uses type-specific `properties`:

- `neogenesis:star` — Two-color gradient (`r0/g0/b0`, `r1/g1/b1`, values 0–255)
- `neogenesis:body` — `atmosphere` object
- `neogenesis:blackhole` — No properties needed (use `{}`)

## Quick Start

A minimal star at the origin (`data/mymod/genesis/celestials/my_sun.json`):

```json
{
  "type": "neogenesis:star",
  "size": 1440,
  "gravity": 2.0,
  "transformProvider": {
    "type": "neogenesis:static",
    "x": 0.0,
    "y": 0.0,
    "z": 0.0
  },
  "properties": {
    "r0": 255, "g0": 220, "b0": 150,
    "r1": 255, "g1": 180, "b1": 80
  }
}
```

This creates celestial `mymod:my_sun`.

## Prerequisites

- A Minecraft instance with Genesis installed
- Basic JSON knowledge
- A text editor

For custom types, renderers, or transform providers: Java mod development environment with Genesis as a dependency.

## Next Steps

- **Adding celestials?** → [Implementation Guide](implementation-guide.md)
- **Custom rendering or orbital mechanics?** → [Advanced Topics](advanced.md)
- **Looking up a specific class or method?** → [API Reference](api-reference.md)
