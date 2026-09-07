# compose-graph-path

Jetpack Compose **Canvas** lab: ten weighted nodes (A–J). Tap two of them to draw a cheapest path and show its cost.

The graph is **undirected**. `MapData.edges` stores each link once as `Edge(from, to, weight)` with node indices `0..9`. `findPath` walks each edge in both directions with the same weight, so tap order (A then J, or J then A) uses the same links.

Pathfinding is **not Dijkstra**. It is a recursive walk of every acyclic route that keeps the lowest total weight. Fine for ten nodes. Returns `null` only if the two nodes are disconnected.

Nodes start in a circle on the canvas. Positions are canvas `Offset`s, not geography.

<p>
<img src="https://github.com/user-attachments/assets/07762bec-8328-4677-947c-6e232360439c" alt="Path on the circle layout" width="220"/>
<img src="https://github.com/user-attachments/assets/4859c02c-533a-486d-a4a8-0a1678f76c85" alt="Path after scrambling nodes" width="220"/>
</p>

| Gesture | Effect |
| --- | --- |
| Tap a node | Select it. First tap is start, second is end. |
| Second tap | Run `findPath`, draw the path in magenta, show `A -> …` and the cost. A third tap starts a new pair. |
| Clear / double-tap | Clear selection and path. Layout stays. |
| Long-press | Scatter nodes; edges and weights stay the same. |

```
MainActivity → GraphScreen (viewModel())
  GraphViewModel   selected ids, path, cost, node positions
  MapData          undirected edge list
  findPath(edges, start, end) -> GraphPath?
```

```bash
./gradlew :app:testDebugUnitTest
```
