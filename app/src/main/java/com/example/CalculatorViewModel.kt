package com.example

import androidx.lifecycle.ViewModel
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.update
import net.objecthunter.exp4j.ExpressionBuilder

data class CalculatorState(
    val expression: String = "",
    val cursorPosition: Int = 0,
    val result: String = "0",
    val itemCount: Int = 0,
    val soulJarItem: String? = null,
    val isFinalized: Boolean = false,
    val showInvertSwitch: Boolean = false,
    val notchMemory: String? = null
)

sealed class CalculatorEvent {
    data class InsertToken(val token: String) : CalculatorEvent()
    object Backspace : CalculatorEvent()
    object Clear : CalculatorEvent()
    object Finalize : CalculatorEvent()
    object Undo : CalculatorEvent()
    object Redo : CalculatorEvent()
    object RestoreSoul : CalculatorEvent()
    object InvertExpression : CalculatorEvent()
    object TiltNotch : CalculatorEvent() // Simulate tilt to store/retrieve memory
    data class MoveCursor(val index: Int) : CalculatorEvent()
    data class DeleteAtIndex(val index: Int) : CalculatorEvent()
}

class CalculatorViewModel : ViewModel() {
    private val _state = MutableStateFlow(CalculatorState())
    val state: StateFlow<CalculatorState> = _state.asStateFlow()

    private val undoStack = mutableListOf<CalculatorState>()
    private val redoStack = mutableListOf<CalculatorState>()

    fun onEvent(event: CalculatorEvent) {
        when (event) {
            is CalculatorEvent.InsertToken -> insertToken(event.token)
            is CalculatorEvent.Backspace -> backspace()
            is CalculatorEvent.Clear -> clear()
            is CalculatorEvent.Finalize -> finalize()
            is CalculatorEvent.Undo -> undo()
            is CalculatorEvent.Redo -> redo()
            is CalculatorEvent.RestoreSoul -> restoreSoul()
            is CalculatorEvent.InvertExpression -> invertExpression()
            is CalculatorEvent.TiltNotch -> tiltNotch()
            is CalculatorEvent.MoveCursor -> moveCursor(event.index)
            is CalculatorEvent.DeleteAtIndex -> deleteAtIndex(event.index)
        }
    }

    private fun saveState() {
        if (undoStack.size > 20) undoStack.removeAt(0)
        undoStack.add(_state.value.copy())
        redoStack.clear()
    }

    private fun insertToken(token: String) {
        saveState()
        val current = _state.value
        var newExpr = current.expression
        var newCursor = current.cursorPosition
        var isFinalized = current.isFinalized

        if (isFinalized) {
            if (token in listOf("+", "-", "*", "/", "%")) {
                newExpr = current.result.replace(",", "")
                newCursor = newExpr.length
                isFinalized = false
            } else {
                newExpr = ""
                newCursor = 0
                isFinalized = false
            }
        }

        val left = newExpr.substring(0, newCursor)
        val right = newExpr.substring(newCursor)
        val lastChar = left.lastOrNull()?.toString()
        val isOp = token in listOf("+", "-", "*", "/", "%")
        val isLastOp = lastChar in listOf("+", "-", "*", "/", "%")

        if (isOp && isLastOp) {
            newExpr = left.dropLast(1) + token + right
        } else {
            // Smart append logic
            if (isFinalized == false && current.expression.isNotEmpty() && current.expression.last().isDigit() && token.first().isDigit() && current.expression == "0" && token != ".") {
                // If just "0" and typed a digit, replace 0
                newExpr = token + right
                newCursor = token.length
            } else {
                newExpr = left + token + right
                newCursor += token.length
            }
        }

        updateStateWithEvaluation(newExpr, newCursor, isFinalized)
    }

    private fun backspace() {
        val current = _state.value
        if (current.cursorPosition == 0) return
        saveState()

        if (current.isFinalized) {
            clear(saveToSoul = false)
            return
        }

        val newExpr = current.expression.removeRange(current.cursorPosition - 1, current.cursorPosition)
        val newCursor = current.cursorPosition - 1
        updateStateWithEvaluation(newExpr, newCursor, current.isFinalized)
    }

    private fun deleteAtIndex(index: Int) {
        val current = _state.value
        if (index < 0 || index >= current.expression.length) return
        saveState()

        val newExpr = current.expression.removeRange(index, index + 1)
        val newCursor = if (current.cursorPosition > index) current.cursorPosition - 1 else current.cursorPosition
        updateStateWithEvaluation(newExpr, newCursor, current.isFinalized)
    }

    private fun clear(saveToSoul: Boolean = true) {
        val current = _state.value
        val soulVal = if (saveToSoul && current.expression.isNotEmpty()) current.expression else current.soulJarItem
        
        _state.update {
            it.copy(
                expression = "",
                cursorPosition = 0,
                result = "0",
                itemCount = 0,
                isFinalized = false,
                showInvertSwitch = false,
                soulJarItem = soulVal
            )
        }
    }

    private fun finalize() {
        val current = _state.value
        if (current.expression.isEmpty()) return
        saveState()
        
        // Ensure latest evaluation is solid before finalizing
        _state.update { it.copy(isFinalized = true) }
    }

    private fun undo() {
        if (undoStack.isNotEmpty()) {
            redoStack.add(_state.value.copy())
            val prevState = undoStack.removeLast()
            _state.value = prevState
        }
    }

    private fun redo() {
        if (redoStack.isNotEmpty()) {
            undoStack.add(_state.value.copy())
            val nextState = redoStack.removeLast()
            _state.value = nextState
        }
    }

    private fun restoreSoul() {
        val current = _state.value
        val soul = current.soulJarItem ?: return
        saveState()

        val newExpr = if (current.expression.isNotEmpty() && current.expression.last().isDigit() && soul.first().isDigit()) {
            current.expression + "+" + soul
        } else {
            current.expression + soul
        }
        val newCursor = newExpr.length
        
        updateStateWithEvaluation(newExpr, newCursor, false)
        _state.update { it.copy(soulJarItem = null) }
    }

    private fun invertExpression() {
        val current = _state.value
        val parts = current.expression.split(Regex("(?<=-)|(?=-)"))
        if (parts.size >= 3) {
            val lastMinusIndex = current.expression.lastIndexOf("-")
            if (lastMinusIndex > 0) {
                val left = current.expression.substring(0, lastMinusIndex)
                val right = current.expression.substring(lastMinusIndex + 1)
                if (left.toDoubleOrNull() != null && right.toDoubleOrNull() != null) {
                    saveState()
                    val newExpr = right + "-" + left
                    val newCursor = newExpr.length
                    updateStateWithEvaluation(newExpr, newCursor, false)
                }
            }
        }
    }

    private fun tiltNotch() {
        val current = _state.value
        if (current.notchMemory == null && current.expression.isNotEmpty()) {
            // Save to memory and clear
            _state.update { it.copy(notchMemory = current.expression) }
            clear(saveToSoul = false)
        } else if (current.notchMemory != null) {
            // Restore from memory
            val mem = current.notchMemory
            saveState()
            val newExpr = if (current.expression.isNotEmpty() && current.expression.last().isDigit() && mem.first().isDigit()) {
                current.expression + "+" + mem
            } else {
                current.expression + mem
            }
            val newCursor = newExpr.length
            updateStateWithEvaluation(newExpr, newCursor, false)
            _state.update { it.copy(notchMemory = null) }
        }
    }

    private fun moveCursor(index: Int) {
        val newCursor = index.coerceIn(0, _state.value.expression.length)
        _state.update { it.copy(cursorPosition = newCursor) }
    }

    private fun updateStateWithEvaluation(expr: String, cursor: Int, finalized: Boolean) {
        val count = expr.split(Regex("[+\\-*/%]")).filter { it.isNotEmpty() }.size
        var resultStr = "0"
        var showInvert = false

        if (expr.isNotEmpty()) {
            try {
                // Pre-process for exp4j: handle % as /100.
                // Simple regex replacement for basic usage (e.g. 50% -> (50/100))
                val safeExpr = expr.replace(Regex("(\\d+(?:\\.\\d+)?)%"), "($1/100)")
                if (!safeExpr.last().let { it == '+' || it == '-' || it == '*' || it == '/' }) {
                    val evalResult = ExpressionBuilder(safeExpr).build().evaluate()
                    
                    // Format to drop .0
                    resultStr = if (evalResult == evalResult.toLong().toDouble()) {
                        evalResult.toLong().toString()
                    } else {
                        String.format("%.8f", evalResult).trimEnd('0').trimEnd('.')
                    }
                    
                    if (evalResult < 0) {
                        showInvert = true
                    }
                }
            } catch (e: Exception) {
                // Ignore evaluation errors while typing
                resultStr = _state.value.result
            }
        }

        _state.update {
            it.copy(
                expression = expr,
                cursorPosition = cursor,
                result = resultStr,
                itemCount = count,
                isFinalized = finalized,
                showInvertSwitch = showInvert
            )
        }
    }
}
