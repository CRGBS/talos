"""Move between two underwater waypoints without mining blocks.

Run in-game with:
    /talos script run safe_two_points

Requires the Talos build where the Python pathFuture uses
PathingOptions.NAVIGATE_ONLY. The built-in pathfinder may swim, jump,
or route around obstacles; it must not mine through them.
"""

import talos

WAYPOINTS = (
    (325, 4, -3198),
    (336.5, 7, -3204.5),
)

for index, (x, y, z) in enumerate(WAYPOINTS, start=1):
    talos.log(f"Moving to waypoint {index}: ({x}, {y}, {z})")
    # goto is awaited before continuing to the next waypoint.
    talos.goto(x, y, z)

talos.log("Both waypoint moves completed.")
talos.chat("Two-waypoint movement complete.")
