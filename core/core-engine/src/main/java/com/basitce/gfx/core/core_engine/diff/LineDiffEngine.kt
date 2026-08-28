package com.basitce.gfx.core.core_engine.diff

enum class DiffType {
    CONTEXT,
    ADD,
    REMOVE
}

data class DiffLine(
    val type: DiffType,
    val oldLineNumber: Int?,
    val newLineNumber: Int?,
    val text: String
)

/**
 * Basit line-based diff engine.
 *
 * Çok büyük dosyalarda memory kullanımını korumak için fallback kullanır.
 */
object LineDiffEngine {

    private const val MAX_PRODUCT = 2_000_000

    fun diff(
        oldText: String,
        newText: String
    ): List<DiffLine> {
        val oldLines = oldText.lines()
        val newLines = newText.lines()

        if (oldLines.size.toLong() * newLines.size.toLong() > MAX_PRODUCT) {
            return simpleDiff(oldLines, newLines)
        }

        val m = oldLines.size
        val n = newLines.size

        val dp = Array(m + 1) { IntArray(n + 1) }

        for (i in m - 1 downTo 0) {
            for (j in n - 1 downTo 0) {
                dp[i][j] = if (oldLines[i] == newLines[j]) {
                    dp[i + 1][j + 1] + 1
                } else {
                    maxOf(
                        dp[i + 1][j],
                        dp[i][j + 1]
                    )
                }
            }
        }

        val result = mutableListOf<DiffLine>()

        var i = 0
        var j = 0

        var oldLineNo = 1
        var newLineNo = 1

        while (i < m && j < n) {
            when {
                oldLines[i] == newLines[j] -> {
                    result.add(
                        DiffLine(
                            type = DiffType.CONTEXT,
                            oldLineNumber = oldLineNo,
                            newLineNumber = newLineNo,
                            text = oldLines[i]
                        )
                    )

                    i++
                    j++
                    oldLineNo++
                    newLineNo++
                }

                dp[i + 1][j] >= dp[i][j + 1] -> {
                    result.add(
                        DiffLine(
                            type = DiffType.REMOVE,
                            oldLineNumber = oldLineNo,
                            newLineNumber = null,
                            text = oldLines[i]
                        )
                    )

                    i++
                    oldLineNo++
                }

                else -> {
                    result.add(
                        DiffLine(
                            type = DiffType.ADD,
                            oldLineNumber = null,
                            newLineNumber = newLineNo,
                            text = newLines[j]
                        )
                    )

                    j++
                    newLineNo++
                }
            }
        }

        while (i < m) {
            result.add(
                DiffLine(
                    type = DiffType.REMOVE,
                    oldLineNumber = oldLineNo,
                    newLineNumber = null,
                    text = oldLines[i]
                )
            )

            i++
            oldLineNo++
        }

        while (j < n) {
            result.add(
                DiffLine(
                    type = DiffType.ADD,
                    oldLineNumber = null,
                    newLineNumber = newLineNo,
                    text = newLines[j]
                )
            )

            j++
            newLineNo++
        }

        return result
    }

    private fun simpleDiff(
        oldLines: List<String>,
        newLines: List<String>
    ): List<DiffLine> {
        val result = mutableListOf<DiffLine>()

        oldLines.forEachIndexed { index, line ->
            result.add(
                DiffLine(
                    type = DiffType.REMOVE,
                    oldLineNumber = index + 1,
                    newLineNumber = null,
                    text = line
                )
            )
        }

        newLines.forEachIndexed { index, line ->
            result.add(
                DiffLine(
                    type = DiffType.ADD,
                    oldLineNumber = null,
                    newLineNumber = index + 1,
                    text = line
                )
            )
        }

        return result
    }
}
