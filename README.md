# compose-graph-path

A small Jetpack Compose **Canvas** lab: ten weighted nodes, tap two of them, draw a path and show its cost.

| Gesture | Effect |
| --- | --- |
| Tap a node | Select it. First tap is start, second is end. |
| Second tap | Compute a path, draw it in magenta, show `A -> C -> …` and the cost. |
| Clear / double-tap | Clear selection and path. |
| Long-press | Scatter nodes; edges stay the same. |

<img width="1080" height="2400" alt="Screenshot_20260907_212615" src="https://github.com/user-attachments/assets/07762bec-8328-4677-947c-6e232360439c" />
<img width="1080" height="2400" alt="Screenshot_20260907_212604" src="https://github.com/user-attachments/assets/4859c02c-533a-486d-a4a8-0a1678f76c85" />

```
MainActivity → GraphScreen (viewModel)
  GraphViewModel   selected ids, path, cost, positions
  MapData          Edge(from, to, weight) with indices 0..9
  findPath         (edges, start, end) -> GraphPath?
```

```bash
./gradlew :app:testDebugUnitTest
```
