package com.ckc.renote

class UndoManager {
    private lateinit var currentState: State
    private lateinit var undoStack: ArrayDeque<State>
    private lateinit var redoStack: ArrayDeque<State>

    fun getCurrentState() = currentState

    fun initState(state: State) {
        currentState = state
        undoStack = ArrayDeque()
        redoStack = ArrayDeque()
    }

    fun addState(state: State) {
        undoStack.addLast(currentState)
        currentState = state
        redoStack.clear()
    }

    fun undo(): State {
        if (undoStack.isNotEmpty()) {
            redoStack.addLast(currentState)
            currentState = undoStack.removeLast()
        }
        return currentState
    }

    fun redo(): State {
        if (redoStack.isNotEmpty()) {
            undoStack.addLast(currentState)
            currentState = redoStack.removeLast()
        }
        return currentState
    }
}
