# compose-graph-path

A small Jetpack Compose **Canvas** lab: ten weighted nodes, tap two of them, draw a path and show its cost.

| Gesture | Effect |
| --- | --- |
| Tap a node | Select it. First tap is start, second is end. |
| Second tap | Compute a path, draw it in magenta, show `A -> C -> …` and the cost. |
| Clear / double-tap | Clear selection and path. |
| Long-press | Scatter nodes; edges stay the same. |

```
MainActivity → GraphScreen (viewModel)
  GraphViewModel   selected ids, path, cost, positions
  MapData          Edge(from, to, weight) with indices 0..9
  findPath         (edges, start, end) -> GraphPath?
```

```bash
./gradlew :app:testDebugUnitTest
```
