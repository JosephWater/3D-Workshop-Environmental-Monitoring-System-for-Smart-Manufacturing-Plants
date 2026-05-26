"""
Generate a smart manufacturing workshop model for the factory monitor project.

Run with Blender:
  blender --background --python scripts/create_workshop_model.py

The script exports:
  frontend/public/models/workshop.glb
"""

from __future__ import annotations

import math
from pathlib import Path

import bpy
from mathutils import Vector


REPO_ROOT = Path(__file__).resolve().parents[1]
OUTPUT_PATH = REPO_ROOT / "frontend" / "public" / "models" / "workshop.glb"

# The Vue/Three.js loader currently scales external models by 1.8.
# Build the Blender scene smaller so the final model aligns with the
# existing sensor coordinates after that runtime scale is applied.
RUNTIME_SCALE = 1.8


def scene_units(value: float) -> float:
    return value / RUNTIME_SCALE


def vec(x: float, y: float, z: float) -> tuple[float, float, float]:
    return (scene_units(x), scene_units(y), scene_units(z))


def size(x: float, y: float, z: float) -> tuple[float, float, float]:
    return (scene_units(x), scene_units(y), scene_units(z))


def clear_scene() -> None:
    bpy.ops.object.select_all(action="SELECT")
    bpy.ops.object.delete()


def make_material(
    name: str,
    color: tuple[float, float, float, float],
    roughness: float = 0.65,
    metallic: float = 0.0,
    alpha: float | None = None,
) -> bpy.types.Material:
    material = bpy.data.materials.new(name)
    material.use_nodes = True
    bsdf = material.node_tree.nodes.get("Principled BSDF")
    bsdf.inputs["Base Color"].default_value = color
    bsdf.inputs["Roughness"].default_value = roughness
    bsdf.inputs["Metallic"].default_value = metallic
    if alpha is not None and alpha < 1.0:
        material.blend_method = "BLEND"
        material.use_screen_refraction = True
        bsdf.inputs["Alpha"].default_value = alpha
    return material


def add_cube(
    name: str,
    location: tuple[float, float, float],
    dimensions: tuple[float, float, float],
    material: bpy.types.Material,
    rotation_z: float = 0.0,
) -> bpy.types.Object:
    bpy.ops.mesh.primitive_cube_add(size=1, location=location, rotation=(0, 0, rotation_z))
    obj = bpy.context.object
    obj.name = name
    obj.dimensions = dimensions
    bpy.ops.object.transform_apply(location=False, rotation=False, scale=True)
    obj.data.materials.append(material)
    return obj


def add_cylinder(
    name: str,
    location: tuple[float, float, float],
    radius: float,
    depth: float,
    material: bpy.types.Material,
    vertices: int = 24,
    rotation: tuple[float, float, float] = (0, 0, 0),
) -> bpy.types.Object:
    bpy.ops.mesh.primitive_cylinder_add(
        vertices=vertices,
        radius=scene_units(radius),
        depth=scene_units(depth),
        location=location,
        rotation=rotation,
    )
    obj = bpy.context.object
    obj.name = name
    obj.data.materials.append(material)
    return obj


def add_label_plate(
    text: str,
    location: tuple[float, float, float],
    material: bpy.types.Material,
    rotation: tuple[float, float, float] = (math.radians(90), 0, 0),
) -> None:
    bpy.ops.object.text_add(location=location, rotation=rotation)
    obj = bpy.context.object
    obj.name = f"Label_{text}"
    obj.data.body = text
    obj.data.align_x = "CENTER"
    obj.data.align_y = "CENTER"
    obj.data.size = scene_units(0.55)
    obj.data.extrude = scene_units(0.015)
    obj.data.materials.append(material)


def build_materials() -> dict[str, bpy.types.Material]:
    return {
        "floor": make_material("mat_floor_light_gray", (0.74, 0.78, 0.82, 1), 0.82, 0.02),
        "floor_dark": make_material("mat_floor_lane", (0.50, 0.54, 0.58, 1), 0.78, 0.06),
        "wall": make_material("mat_wall_warm_white", (0.88, 0.91, 0.94, 1), 0.86, 0.0),
        "steel": make_material("mat_brushed_steel", (0.56, 0.60, 0.64, 1), 0.48, 0.42),
        "dark_steel": make_material("mat_dark_steel", (0.25, 0.28, 0.31, 1), 0.55, 0.38),
        "blue": make_material("mat_machine_blue", (0.10, 0.34, 0.68, 1), 0.5, 0.16),
        "amber": make_material("mat_safety_amber", (0.95, 0.61, 0.10, 1), 0.48, 0.08),
        "green": make_material("mat_status_green", (0.20, 0.66, 0.44, 1), 0.5, 0.05),
        "red": make_material("mat_emergency_red", (0.76, 0.12, 0.10, 1), 0.45, 0.08),
        "black": make_material("mat_rubber_black", (0.04, 0.05, 0.06, 1), 0.7, 0.02),
        "glass": make_material("mat_translucent_glass", (0.70, 0.88, 1.0, 0.32), 0.2, 0.0, 0.32),
        "white": make_material("mat_label_white", (0.95, 0.96, 0.98, 1), 0.7, 0.0),
    }


def build_shell(mat: dict[str, bpy.types.Material]) -> None:
    add_cube("Floor_Slab", vec(0, 0, -0.12), size(46, 46, 0.24), mat["floor"])
    add_cube("Back_Wall", vec(0, -23, 3.8), size(46, 0.55, 7.6), mat["wall"])
    add_cube("Left_Wall", vec(-23, 0, 3.8), size(0.55, 46, 7.6), mat["wall"])
    add_cube("Right_Wall", vec(23, 0, 3.8), size(0.55, 46, 7.6), mat["wall"])

    for x in (-20, -10, 0, 10, 20):
        add_cube(f"Roof_Beam_X_{x}", vec(x, 0, 8.5), size(0.38, 46, 0.35), mat["steel"])
    for y in (-20, -10, 0, 10, 20):
        add_cube(f"Roof_Beam_Y_{y}", vec(0, y, 8.85), size(46, 0.35, 0.35), mat["steel"])
    for x in (-22, 22):
        for y in (-22, 22):
            add_cube(f"Corner_Pillar_{x}_{y}", vec(x, y, 4.15), size(0.65, 0.65, 8.3), mat["dark_steel"])

    for offset in (-15, 0, 15):
        add_cube(f"Floor_Grid_X_{offset}", vec(offset, 0, 0.015), size(0.06, 44, 0.035), mat["white"])
        add_cube(f"Floor_Grid_Y_{offset}", vec(0, offset, 0.015), size(44, 0.06, 0.035), mat["white"])


def build_conveyor(mat: dict[str, bpy.types.Material]) -> None:
    add_cube("Main_Conveyor_Base", vec(3, 5, 0.55), size(31, 4.0, 0.45), mat["dark_steel"], math.radians(-33))
    add_cube("Main_Conveyor_Belt", vec(3, 5, 0.86), size(31, 3.25, 0.18), mat["black"], math.radians(-33))
    for i, offset in enumerate(range(-14, 15, 4)):
        add_cube(f"Conveyor_Roller_{i}", vec(3 + offset * 0.84, 5 - offset * 0.55, 1.0), size(0.22, 3.5, 0.22), mat["steel"], math.radians(-33))
    for x in (-9, 0, 9, 16):
        add_cylinder("Conveyor_Support", vec(x, 5 - x * 0.55, 0.42), 0.08, 0.85, mat["steel"])


def build_press_machine(index: int, x: float, y: float, mat: dict[str, bpy.types.Material]) -> None:
    prefix = f"Press_{index:02d}"
    add_cube(f"{prefix}_Base", vec(x, y, 0.35), size(5.2, 3.4, 0.7), mat["steel"])
    add_cube(f"{prefix}_Blue_Body", vec(x, y, 1.1), size(4.1, 2.45, 0.75), mat["blue"])
    add_cube(f"{prefix}_Frame_Left", vec(x - 1.75, y, 2.85), size(0.55, 2.65, 3.5), mat["dark_steel"])
    add_cube(f"{prefix}_Frame_Right", vec(x + 1.75, y, 2.85), size(0.55, 2.65, 3.5), mat["dark_steel"])
    add_cube(f"{prefix}_Top_Crosshead", vec(x, y, 4.65), size(4.4, 2.75, 0.55), mat["steel"])
    add_cube(f"{prefix}_Ram", vec(x, y, 3.1), size(2.25, 1.4, 0.65), mat["amber"])
    add_cube(f"{prefix}_Panel", vec(x + 2.35, y - 0.55, 2.1), size(0.22, 1.2, 1.6), mat["black"])
    add_cube(f"{prefix}_Status_Green", vec(x + 2.48, y - 0.78, 2.55), size(0.08, 0.24, 0.24), mat["green"])
    add_cube(f"{prefix}_Status_Red", vec(x + 2.48, y - 0.36, 2.55), size(0.08, 0.24, 0.24), mat["red"])
    add_label_plate(f"B{20 + index}", vec(x, y - 1.73, 1.58), mat["white"])


def build_equipment(mat: dict[str, bpy.types.Material]) -> None:
    build_conveyor(mat)
    for index, (x, y) in enumerate([(-12, -5), (-5, -1), (2, 3), (9, 7)], start=1):
        build_press_machine(index, x, y, mat)

    for i, (x, y) in enumerate([(15, -12), (18, -12), (21, -12)], start=1):
        add_cube(f"Control_Cabinet_{i}", vec(x, y, 1.65), size(1.6, 0.85, 3.3), mat["dark_steel"])
        add_cube(f"Control_Cabinet_{i}_Door", vec(x, y - 0.44, 1.85), size(1.25, 0.04, 2.35), mat["blue"])
        add_cube(f"Control_Cabinet_{i}_Light", vec(x + 0.45, y - 0.49, 2.75), size(0.25, 0.04, 0.25), mat["green"])

    add_cube("Glass_Partition", vec(14, 12, 2.35), size(0.12, 8.5, 4.7), mat["glass"])
    add_cube("Glass_Frame_Left", vec(14, 7.8, 2.35), size(0.2, 0.2, 4.9), mat["steel"])
    add_cube("Glass_Frame_Right", vec(14, 16.2, 2.35), size(0.2, 0.2, 4.9), mat["steel"])
    add_cube("Inspection_Table", vec(18, 14, 0.8), size(9.0, 2.8, 0.45), mat["steel"])

    for y in (-16, -8, 0, 8, 16):
        add_cylinder("Overhead_Pipe_X", vec(0, y, 6.9), 0.08, 42, mat["steel"], vertices=16, rotation=(0, math.radians(90), 0))
    for x in (-16, -8, 0, 8, 16):
        add_cylinder("Overhead_Pipe_Y", vec(x, 0, 7.25), 0.06, 42, mat["dark_steel"], vertices=16, rotation=(math.radians(90), 0, 0))

    add_cube("Safety_Walkway", vec(-9, 9, 0.025), size(22, 3.2, 0.04), mat["floor_dark"], math.radians(-33))
    add_cube("Yellow_Safety_Line_A", vec(-9, 7.35, 0.06), size(22, 0.12, 0.06), mat["amber"], math.radians(-33))
    add_cube("Yellow_Safety_Line_B", vec(-9, 10.65, 0.06), size(22, 0.12, 0.06), mat["amber"], math.radians(-33))


def add_lighting_and_camera() -> None:
    bpy.ops.object.light_add(type="AREA", location=vec(0, -8, 12))
    key = bpy.context.object
    key.name = "Large_Softbox_Light"
    key.data.energy = 500
    key.data.size = scene_units(24)

    bpy.ops.object.light_add(type="SUN", location=vec(12, -18, 14))
    sun = bpy.context.object
    sun.name = "Workshop_Sun_Light"
    sun.data.energy = 1.4
    sun.rotation_euler = (math.radians(45), 0, math.radians(35))

    bpy.ops.object.camera_add(location=vec(30, -34, 22), rotation=(math.radians(60), 0, math.radians(40)))
    camera = bpy.context.object
    bpy.context.scene.camera = camera
    direction = Vector(vec(0, 0, 3)) - camera.location
    camera.rotation_euler = direction.to_track_quat("-Z", "Y").to_euler()


def export_model() -> None:
    OUTPUT_PATH.parent.mkdir(parents=True, exist_ok=True)
    bpy.ops.export_scene.gltf(
        filepath=str(OUTPUT_PATH),
        export_format="GLB",
        export_apply=True,
        export_yup=True,
        export_materials="EXPORT",
        export_cameras=False,
        export_lights=False,
    )


def main() -> None:
    clear_scene()
    materials = build_materials()
    build_shell(materials)
    build_equipment(materials)
    add_lighting_and_camera()
    export_model()
    print(f"Exported workshop model to: {OUTPUT_PATH}")


if __name__ == "__main__":
    main()
