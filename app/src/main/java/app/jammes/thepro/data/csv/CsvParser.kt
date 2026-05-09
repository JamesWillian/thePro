package app.jammes.thepro.data.csv

import app.jammes.thepro.domain.model.Exercise
import app.jammes.thepro.domain.model.ExerciseType
import app.jammes.thepro.domain.repository.WorkoutImportRow
import java.io.BufferedReader
import java.io.InputStream
import java.io.InputStreamReader
import javax.inject.Inject
import javax.inject.Singleton

/**
 * Parser CSV simples seguindo RFC 4180 (vírgula ou ponto-e-vírgula como separador,
 * suporte a aspas duplas e escape ""). Tolerante a cabeçalhos em PT-BR ou EN.
 */
@Singleton
class CsvParser @Inject constructor() {

    fun parseExercises(input: InputStream): List<Exercise> {
        val rows = readRows(input)
        if (rows.isEmpty()) return emptyList()
        val header = rows.first().map { it.normalizeHeader() }
        val nameIdx = header.indexOfFirstOf("nome", "name")
        val descIdx = header.indexOfFirstOf("descricao", "descrição", "description", "desc")
        val typeIdx = header.indexOfFirstOf("tipo", "type", "categoria")
        require(nameIdx >= 0) { "CSV inválido: coluna 'nome' não encontrada" }

        return rows.drop(1).mapNotNull { row ->
            val name = row.getOrNull(nameIdx)?.trim().orEmpty()
            if (name.isBlank()) return@mapNotNull null
            Exercise(
                name = name,
                description = row.getOrNull(descIdx)?.trim().orEmpty(),
                type = ExerciseType.fromString(row.getOrNull(typeIdx))
            )
        }
    }

    fun parseWorkouts(input: InputStream): List<WorkoutImportRow> {
        val rows = readRows(input)
        if (rows.isEmpty()) return emptyList()
        val header = rows.first().map { it.normalizeHeader() }

        val workoutNameIdx = header.indexOfFirstOf("treino", "workout", "workout_name", "nome_treino")
        val workoutDescIdx = header.indexOfFirstOf("descricao_treino", "workout_description", "descricao", "descrição")
        val exerciseIdx = header.indexOfFirstOf("exercicio", "exercise", "exercise_name", "nome_exercicio")
        val setsIdx = header.indexOfFirstOf("series", "séries", "sets")
        val repsIdx = header.indexOfFirstOf("repeticoes", "repetições", "reps", "rep")
        val weightIdx = header.indexOfFirstOf("carga", "peso", "weight", "weight_kg")
        val durationIdx = header.indexOfFirstOf("duracao", "duração", "tempo", "duration", "duration_seconds")
        val orderIdx = header.indexOfFirstOf("ordem", "order", "order_index")

        require(workoutNameIdx >= 0) { "CSV inválido: coluna 'treino' não encontrada" }
        require(exerciseIdx >= 0) { "CSV inválido: coluna 'exercicio' não encontrada" }
        require(setsIdx >= 0) { "CSV inválido: coluna 'series' não encontrada" }

        return rows.drop(1).mapIndexedNotNull { idx, row ->
            val workoutName = row.getOrNull(workoutNameIdx)?.trim().orEmpty()
            val exerciseName = row.getOrNull(exerciseIdx)?.trim().orEmpty()
            if (workoutName.isBlank() || exerciseName.isBlank()) return@mapIndexedNotNull null
            WorkoutImportRow(
                workoutName = workoutName,
                workoutDescription = row.getOrNull(workoutDescIdx)?.trim().orEmpty(),
                exerciseName = exerciseName,
                sets = row.getOrNull(setsIdx)?.toIntOrZero() ?: 1,
                reps = row.getOrNull(repsIdx)?.toIntOrNullSafe(),
                weightKg = row.getOrNull(weightIdx)?.toDoubleOrNullSafe(),
                durationSeconds = row.getOrNull(durationIdx)?.toIntOrNullSafe(),
                orderIndex = row.getOrNull(orderIdx)?.toIntOrNullSafe() ?: idx
            )
        }
    }

    private fun readRows(input: InputStream): List<List<String>> {
        BufferedReader(InputStreamReader(input, Charsets.UTF_8)).use { reader ->
            val text = reader.readText().replace("﻿", "")
            if (text.isBlank()) return emptyList()
            val separator = detectSeparator(text)
            return parseCsv(text, separator)
        }
    }

    private fun detectSeparator(text: String): Char {
        val firstLine = text.lineSequence().firstOrNull().orEmpty()
        val commas = firstLine.count { it == ',' }
        val semis = firstLine.count { it == ';' }
        return if (semis > commas) ';' else ','
    }

    private fun parseCsv(text: String, sep: Char): List<List<String>> {
        val rows = mutableListOf<List<String>>()
        val current = StringBuilder()
        var row = mutableListOf<String>()
        var insideQuotes = false
        var i = 0
        while (i < text.length) {
            val c = text[i]
            when {
                insideQuotes -> {
                    if (c == '"') {
                        if (i + 1 < text.length && text[i + 1] == '"') {
                            current.append('"'); i++
                        } else insideQuotes = false
                    } else current.append(c)
                }
                c == '"' -> insideQuotes = true
                c == sep -> { row.add(current.toString()); current.clear() }
                c == '\r' -> { /* skip */ }
                c == '\n' -> {
                    row.add(current.toString()); current.clear()
                    if (row.any { it.isNotBlank() }) rows.add(row)
                    row = mutableListOf()
                }
                else -> current.append(c)
            }
            i++
        }
        if (current.isNotEmpty() || row.isNotEmpty()) {
            row.add(current.toString())
            if (row.any { it.isNotBlank() }) rows.add(row)
        }
        return rows
    }

    private fun String.normalizeHeader(): String =
        trim().lowercase()
            .replace("\"", "")
            .replace(" ", "_")

    private fun List<String>.indexOfFirstOf(vararg keys: String): Int {
        keys.forEach { k ->
            val idx = indexOfFirst { it == k }
            if (idx >= 0) return idx
        }
        return -1
    }

    private fun String.toIntOrZero(): Int = trim().replace(",", ".").toDoubleOrNull()?.toInt() ?: 0
    private fun String.toIntOrNullSafe(): Int? {
        val v = trim()
        if (v.isBlank()) return null
        return v.replace(",", ".").toDoubleOrNull()?.toInt()
    }
    private fun String.toDoubleOrNullSafe(): Double? {
        val v = trim()
        if (v.isBlank()) return null
        return v.replace(",", ".").toDoubleOrNull()
    }
}
