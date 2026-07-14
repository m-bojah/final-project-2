# Java Chess Game

A complete desktop chess game built with pure Java (Java 17+) and Swing — no external libraries required.

## Features

| Category | Details |
|---|---|
| **Rules** | Legal moves, check/checkmate/stalemate, castling, en passant, pawn promotion |
| **AI** | Easy (random), Medium (2-ply search), Hard (4-ply minimax + alpha-beta pruning) |
| **GUI** | Clickable 8×8 board, Unicode pieces, highlighted legal moves, last-move indicator |
| **Timers** | Individual countdown clocks with low-time warnings |
| **History** | Scrollable algebraic-notation move list |
| **Undo/Redo** | Takes back/replays complete human+AI move pairs |
| **Save/Load** | Serializes full game state to `.chess` files |
| **Statistics** | Tracks wins/losses/draws per difficulty, persisted across sessions |
| **Sound** | Synthesized move/capture/check/checkmate beeps (toggleable) |
| **Themes** | Light and dark board themes |


## Package Structure

```
src/com/chess/
├── main/         Main.java              — entry point
├── model/        Board, Piece hierarchy, Move, Position, …
├── ai/           AIPlayer, BoardEvaluator, Difficulty
├── controller/   GameManager            — game orchestration
├── view/         ChessGUI, BoardPanel, StatusPanel, …
├── persistence/  SaveManager, StatisticsManager
└── utils/        SoundManager, ThemeManager
```

## Architecture

- **Model** — pure game logic with no UI dependencies. `Board` implements full chess rules including make/unmake for efficient AI search.
- **AI** — negamax with alpha-beta pruning and piece-square table evaluation. Hard depth = 4 plies.
- **Controller** (`GameManager`) — coordinates model and view, runs AI in a background `SwingWorker`, manages timers and undo/redo stacks.
- **View** — Swing panels; `BoardPanel` converts mouse clicks to `Position` objects and delegates to `GameManager`.

## Controls

| Action | How |
|---|---|
| Select piece | Left-click |
| Move piece | Click destination (green dot = quiet, ring = capture) |
| Pawn promotion | Dialog appears automatically |
| Undo | Undo button (takes back your move + AI response) |
| New Game | Game menu — choose difficulty |
| Save / Load | Buttons in side panel |
