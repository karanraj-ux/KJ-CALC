package com.example

import androidx.compose.ui.text.TextRange
import androidx.compose.ui.text.input.TextFieldValue
import androidx.lifecycle.ViewModel
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.update

enum class AppMode {
    CALCULATOR, CONNECT, TIC_TAC_TOE
}

data class HistoryItem(val expression: String, val result: String)

data class AppState(
    val mode: AppMode = AppMode.CALCULATOR,
    val expression: TextFieldValue = TextFieldValue(""),
    val resultPreview: String = "",
    val history: List<HistoryItem> = emptyList(),
    val showHistory: Boolean = false,
    
    // Connect mode
    val connectionCode: String = "",
    val isConnected: Boolean = false,

    // Tic Tac Toe State
    val tttBoard: List<String> = List(9) { "" }, // Empty spots
    val isXTurn: Boolean = true,
    val gameWinner: String? = null // "X", "O", "Draw", or null
)

class MainViewModel : ViewModel() {
    private val _state = MutableStateFlow(AppState())
    val state = _state.asStateFlow()

    fun updateExpression(newValue: TextFieldValue) {
        _state.update { 
            val preview = try {
                val evaluated = CalculatorLogic.evaluate(newValue.text)
                if (evaluated != "Error" && evaluated.isNotEmpty()) evaluated else ""
            } catch (e: Exception) { "" }
            it.copy(expression = newValue, resultPreview = preview)
        }
    }

    fun onKeyPadPress(key: String) {
        val currentText = _state.value.expression.text
        val selectionStart = _state.value.expression.selection.start
        val selectionEnd = _state.value.expression.selection.end
        
        val newText = currentText.substring(0, selectionStart) + key + currentText.substring(selectionEnd)
        val newPos = selectionStart + key.length
        
        updateExpression(TextFieldValue(newText, TextRange(newPos)))
    }

    fun onBackspace() {
        val currentText = _state.value.expression.text
        if (currentText.isEmpty()) return
        
        val selectionStart = _state.value.expression.selection.start
        val selectionEnd = _state.value.expression.selection.end
        
        if (selectionStart == selectionEnd && selectionStart > 0) {
            val newText = currentText.substring(0, selectionStart - 1) + currentText.substring(selectionEnd)
            updateExpression(TextFieldValue(newText, TextRange(selectionStart - 1)))
        } else if (selectionStart != selectionEnd) {
            val newText = currentText.substring(0, selectionStart) + currentText.substring(selectionEnd)
            updateExpression(TextFieldValue(newText, TextRange(selectionStart)))
        }
    }

    fun onClear() {
        updateExpression(TextFieldValue(""))
        _state.update { it.copy(resultPreview = "") }
    }

    fun onEquals() {
        val st = _state.value
        if (st.expression.text.isEmpty()) return
        val res = CalculatorLogic.evaluate(st.expression.text)
        if (res != "Error") {
            val safeRes = if (res.endsWith(".0")) res.dropLast(2) else res
            
            val newHistory = listOf(HistoryItem(st.expression.text, safeRes)) + st.history
            _state.update {
                it.copy(
                    history = newHistory,
                    expression = TextFieldValue(text = safeRes, selection = TextRange(safeRes.length)),
                    resultPreview = ""
                )
            }
        }
    }

    fun toggleHistory() {
        _state.update { it.copy(showHistory = !it.showHistory) }
    }

    fun setMode(mode: AppMode) {
        _state.update { it.copy(mode = mode) }
    }

    fun onConnectCodeChange(code: String) {
        _state.update { it.copy(connectionCode = code) }
    }

    fun connect() {
        // Mock connection
        _state.update { 
            it.copy(
                isConnected = true, 
                mode = AppMode.TIC_TAC_TOE,
                tttBoard = List(9) { "" },
                isXTurn = true,
                gameWinner = null
            )
        }
    }

    fun playTicTacToe(index: Int) {
        val st = _state.value
        if (st.gameWinner != null || st.tttBoard[index].isNotEmpty()) return

        val newBoard = st.tttBoard.toMutableList()
        newBoard[index] = if (st.isXTurn) "X" else "O"
        
        val winner = checkWinner(newBoard)

        _state.update {
            it.copy(
                tttBoard = newBoard,
                isXTurn = !it.isXTurn,
                gameWinner = winner
            )
        }
    }

    fun resetGame() {
        _state.update { 
            it.copy(
                tttBoard = List(9) { "" },
                isXTurn = true,
                gameWinner = null
            )
        }
    }

    private fun checkWinner(board: List<String>): String? {
        val lines = listOf(
            listOf(0, 1, 2), listOf(3, 4, 5), listOf(6, 7, 8), // rows
            listOf(0, 3, 6), listOf(1, 4, 7), listOf(2, 5, 8), // cols
            listOf(0, 4, 8), listOf(2, 4, 6) // diagonals
        )
        for (line in lines) {
            val a = board[line[0]]
            val b = board[line[1]]
            val c = board[line[2]]
            if (a.isNotEmpty() && a == b && a == c) return a
        }
        if (board.all { it.isNotEmpty() }) return "Draw"
        return null
    }
}
