# compose-graph-path

Jetpack Compose **Canvas** lab: a small **weighted graph** on the screen, tap two nodes, draw a path and show its cost.

This is **not** GPS, Maps, or device location. There are no location permissions. “GPS” in the old repo name only meant “points that look a bit like lat/lng.” Random pairs are mapped onto pixels so they can be drawn.

Pathfinding here is **not Dijkstra**. It is a recursive walk of every forward path that keeps the cheapest total. Fine for ten nodes. Wrong algorithm for a real map.

## What you can do

| Gesture | Effect |
| --- | --- |
| Tap near a label | Select that node (green). Max **two**. |
| Second tap | Compute a path, draw it in **magenta**, show `A -> C -> …` and the cost above the canvas. |
| Double-tap | Clear selection and path. |
| Long-press | New random point layout; graph connections stay the same. |

Nodes are **A–J**. Edges and weights live in `MapData.graph` (adjacency list). Example: A connects to B (weight 1) and D (weight 3).

## How it is built

```
MainActivity
  └─ SurfaceMap          # details panel + canvas
       ├─ PathDetails    # path string + cost
       └─ PointsMap      # Canvas: nodes, edges, hit-testing, path stroke
```

| Piece | Role |
| --- | --- |
| `MapData` | Labels, `maxSelectable = 2`, hardcoded graph |
| `ScreenPointsViewModel` | Random lat/lng-style pairs → screen `x,y` |
| `PointsSelectedViewModel` | Indices of the tapped nodes |
| `PathViewModel` | Runs the search, holds path indices + cost |
| `PointsUtil.isPointNear` | Tap hit-test (50px radius) |

Each Compose screen currently does `PathViewModel()` / `ScreenPointsViewModel()` itself, so these ViewModels **do not** survive rotation the way `viewModel()` would. That is a lab shortcut.

## Path search (read this before calling it Dijkstra)

`PathViewModel.searchDepthListing` / `buildPathPair`:

1. Start at the first selected index.
2. Recurse along outgoing edges (`offset` + `weight` pairs).
3. Build a string of letters (`A` + offset → `B`, …).
4. If this edge lands on the end node and the total weight is smaller than `minDepth`, keep it.

No priority queue, no distance array, no visited set. The graph is a small **forward** chain, so “try all paths, remember the min” usually matches the shortest path. Add cycles or hundreds of nodes and this approach falls over.

**If you are learning Dijkstra:** keep the Canvas and `MapData`, replace `searchDepthListing` with Dijkstra (distances + a min-heap). That is the useful upgrade.

## Run

Open the project in Android Studio and run `app`.

```bash
./gradlew :app:assembleDebug
```

## Suggested next steps

- Implement Dijkstra (or BFS if you drop weights) and compare the magenta path.
- Use `viewModel()` so state survives rotation.
- Drive the graph from a file instead of `MapData`.
- Add a unit test for the search with a tiny fixture graph (no Compose).
