package app.jammes.thepro.domain.model

import java.time.DayOfWeek

enum class DayOfWeekBr(val isoValue: Int, val shortLabel: String, val longLabel: String) {
    SEGUNDA(1, "Seg", "Segunda-feira"),
    TERCA(2, "Ter", "Terça-feira"),
    QUARTA(3, "Qua", "Quarta-feira"),
    QUINTA(4, "Qui", "Quinta-feira"),
    SEXTA(5, "Sex", "Sexta-feira"),
    SABADO(6, "Sáb", "Sábado"),
    DOMINGO(7, "Dom", "Domingo");

    fun toJavaDayOfWeek(): DayOfWeek = DayOfWeek.of(isoValue)

    companion object {
        fun fromIso(iso: Int): DayOfWeekBr =
            entries.firstOrNull { it.isoValue == iso } ?: SEGUNDA

        fun fromJavaDayOfWeek(day: DayOfWeek): DayOfWeekBr = fromIso(day.value)

        val ordered: List<DayOfWeekBr> = listOf(SEGUNDA, TERCA, QUARTA, QUINTA, SEXTA, SABADO, DOMINGO)
    }
}
