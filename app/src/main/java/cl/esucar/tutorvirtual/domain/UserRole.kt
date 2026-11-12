package cl.esucar.tutorvirtual.domain

/** Perfiles de la app (caso ESUCAR, sección 3.1): estudiante y profesor guía. */
enum class UserRole(val label: String) {
    ESTUDIANTE("Estudiante"),
    PROFESOR("Profesor guía");

    companion object {
        /**
         * Código institucional para crear una cuenta docente. El MVP no tiene servidor (caso, sección 3.4),
         * por lo que la validación es local; en la EFT debe pasar a validarse en el backend.
         */
        const val TEACHER_ACCESS_CODE = "ESUCAR-DOCENTE"

        fun from(name: String?): UserRole = entries.firstOrNull { it.name == name } ?: ESTUDIANTE
    }
}
