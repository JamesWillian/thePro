package app.jammes.thepro.domain.model

enum class ExerciseType(val displayName: String) {
    MUSCULACAO("Musculação"),
    CARDIO("Cardio"),
    CORE("Core"),
    FLEXIBILIDADE("Flexibilidade"),
    FUNCIONAL("Funcional"),
    OUTRO("Outro");

    companion object {
        fun fromString(value: String?): ExerciseType {
            if (value.isNullOrBlank()) return OUTRO
            val normalized = value.trim().uppercase()
                .replace("Ç", "C")
                .replace("Ã", "A")
                .replace("Á", "A")
                .replace("É", "E")
                .replace("Í", "I")
                .replace("Ó", "O")
                .replace("Ú", "U")
            return entries.firstOrNull { it.name == normalized }
                ?: entries.firstOrNull { it.displayName.uppercase().startsWith(normalized) }
                ?: OUTRO
        }
    }
}
