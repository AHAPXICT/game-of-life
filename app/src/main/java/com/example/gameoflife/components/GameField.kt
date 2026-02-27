import androidx.compose.foundation.Canvas
import androidx.compose.foundation.border
import androidx.compose.foundation.gestures.detectTapGestures
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxHeight
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.Text
import androidx.compose.runtime.*
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.geometry.Size
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.RectangleShape
import androidx.compose.ui.input.pointer.pointerInput
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import com.example.gameoflife.ui.theme.PrimaryColor
import kotlinx.coroutines.GlobalScope
import kotlinx.coroutines.delay
import kotlinx.coroutines.launch
import kotlin.math.min

@Composable
fun GameField(
    modifier: Modifier = Modifier,
    rows: MutableState<Int>,
    cols: MutableState<Int>,
    cells: MutableMap<Pair<Int, Int>, Boolean>
) {

    var isGameActive by remember { mutableStateOf(false) }

    LaunchedEffect(rows.value, cols.value) {
        val newKeys = buildSet {
            for (row in 0 until rows.value) {
                for (col in 0 until cols.value) {
                    add(row to col)
                }
            }
        }

        // удаляем лишние
        val toRemove = cells.keys - newKeys
        toRemove.forEach { cells.remove(it) }

        // добавляем недостающие (с дефолтным значением)
        newKeys.forEach { key ->
            cells.putIfAbsent(key, false)
        }
    }

    Column(modifier = modifier) {
        Canvas(
            modifier = Modifier
                .fillMaxSize()
                .weight(0.94f)
                .pointerInput(Unit) {
                    detectTapGestures { offset ->
                        val cellSize = min(
                            size.width / cols.value,
                            size.height / rows.value
                        )

                        val fieldWidth = cellSize * cols.value
                        val fieldHeight = cellSize * rows.value

                        val offsetX = (size.width - fieldWidth) / 2f
                        val offsetY = (size.height - fieldHeight) / 2f

                        val localX = offset.x - offsetX
                        val localY = offset.y - offsetY

                        if (localX < 0 || localY < 0) return@detectTapGestures

                        val col = (localX / cellSize).toInt()
                        val row = (localY / cellSize).toInt()

                        if (row in 0 until rows.value && col in 0 until cols.value) {
                            cells[row to col]?.let { cells[row to col] = !it }
                        }
                    }
                }
        ) {
            val cellSize = min(
                size.width / cols.value,
                size.height / rows.value
            )

            val fieldWidth = cellSize * cols.value
            val fieldHeight = cellSize * rows.value

            val offsetX = (size.width - fieldWidth) / 2f
            val offsetY = (size.height - fieldHeight) / 2f

            for (row in 0 until rows.value) {
                for (col in 0 until cols.value) {
                    drawRect(
                        color = if (cells[row to col] == true) Color.White else Color.Black,
                        topLeft = Offset(
                            x = offsetX + col * cellSize,
                            y = offsetY + row * cellSize
                        ),
                        size = Size(cellSize, cellSize)
                    )
                }
            }
            val strokeWidth = 0.5.dp.toPx()
            val gridColor = Color.White

            for (col in 0..cols.value) {
                val x = offsetX + col * cellSize
                drawLine(
                    color = gridColor,
                    start = Offset(x, offsetY),
                    end = Offset(x, offsetY + fieldHeight),
                    strokeWidth = strokeWidth
                )
            }

            for (row in 0..rows.value) {
                val y = offsetY + row * cellSize
                drawLine(
                    color = gridColor,
                    start = Offset(offsetX, y),
                    end = Offset(offsetX + fieldWidth, y),
                    strokeWidth = strokeWidth
                )
            }
        }
        fun oneMove() {
            isGameActive = false
            gameTact(cells, rows.value, cols.value)
        }

        fun startGame() {
            isGameActive = !isGameActive
            GlobalScope.launch {
                while(isGameActive) {
                    gameTact(cells, rows.value, cols.value)
                    delay(500L)
                }
            }
        }

        fun clear() {
            isGameActive = false
            val newKeys = buildSet {
                for (row in 0 until rows.value) {
                    for (col in 0 until cols.value) {
                        add(row to col)
                    }
                }
            }
            newKeys.forEach { key ->
                cells.put(key, false)
            }
        }
        Row(
            Modifier
                .fillMaxWidth()
                .weight(0.06f)
                .clip(RoundedCornerShape(topStart = 10.dp, topEnd = 10.dp))
        ) {
            GameButton(Modifier.weight(1f), { clear() }, "Clear")
            GameButton(Modifier.weight(1f), { oneMove() }, "One move")
            GameButton(Modifier.weight(1f), { startGame() }, "Start game")
        }
    }
}

fun gameTact(map: MutableMap<Pair<Int, Int>, Boolean>, rows: Int, cols: Int) {
    val neighboursMap: MutableMap<Pair<Int, Int>, Int> = mutableMapOf<Pair<Int, Int>, Int>()
    val newKeys = buildSet {
        for (row in 0 until rows) {
            for (col in 0 until cols) {
                add(row to col)
            }
        }
    }
    newKeys.forEach { key ->
        neighboursMap.put(key, 0)
    }
    neighboursMap.keys.forEach { key ->
        neighboursMap.put(key, neighboursCount(key.first, key.second, rows - 1, cols - 1, map))
    }

    neighboursMap.keys.forEach { key ->
        val count = neighboursMap.getValue(key)
        if (map.getValue(key)) {
            if (count < 2 || count > 3) {
                map.put(key, false)
            }
        } else {
            if (count == 3) {
                map.put(key, true)
            }
        }
    }
}

fun neighboursCount(
    row: Int,
    col: Int,
    rows: Int,
    cols: Int,
    map: MutableMap<Pair<Int, Int>, Boolean>
): Int {
    var count = 0;

    for (i in row - 1..row + 1) {
        for (j in col - 1..col + 1) {

            val curRow = if (i < 0) rows else if (i > rows) 0 else i
            val curCol = if (j < 0) cols else if (j > cols) 0 else j

            if (i == row && j == col) continue

            count += if (map.getValue(Pair(curRow, curCol))) 1 else 0
        }
    }

    return count
}

@Composable
fun GameButton(modifier: Modifier, onClick: () -> Unit, text: String) {
    Button(
        onClick,
        shape = RectangleShape,
        colors = ButtonDefaults.buttonColors(PrimaryColor),
        modifier = modifier
            .fillMaxHeight()
            .border(width = 1.dp, color = Color.Black)
    ) {
        Text(text)
    }
}