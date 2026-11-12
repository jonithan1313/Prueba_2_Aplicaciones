package cl.esucar.tutorvirtual

import cl.esucar.tutorvirtual.domain.DeliverableStatus
import cl.esucar.tutorvirtual.domain.UserRole
import cl.esucar.tutorvirtual.domain.Validators
import org.junit.Assert.assertEquals
import org.junit.Assert.assertFalse
import org.junit.Assert.assertNotNull
import org.junit.Assert.assertNull
import org.junit.Assert.assertTrue
import org.junit.Test

class TeacherValidatorsTest {
    @Test fun codigo_docente_correcto_e_incorrecto() {
        assertNull(Validators.teacherCode(UserRole.TEACHER_ACCESS_CODE))
        assertNull(Validators.teacherCode("  esucar-docente "))
        assertNotNull(Validators.teacherCode("OTRO-CODIGO"))
        assertNotNull(Validators.teacherCode(""))
    }

    @Test fun titulo_descripcion_y_formato_de_etapa() {
        assertNotNull(Validators.stageTitle(""))
        assertNotNull(Validators.stageTitle("abc"))
        assertNull(Validators.stageTitle("Marco teórico"))
        assertNotNull(Validators.stageDescription("muy corta"))
        assertNull(Validators.stageDescription("Reúne los conceptos y estudios previos del trabajo."))
        assertNotNull(Validators.stageFormat("PDF"))
        assertNull(Validators.stageFormat("Documento PDF de 4 a 6 páginas"))
    }

    @Test fun fecha_limite_dd_MM_aaaa() {
        assertNull(Validators.deadline("15/11/2026"))
        assertNotNull(Validators.deadline(""))
        assertNotNull(Validators.deadline("2026-11-15"))
        assertNotNull(Validators.deadline("31/02/2026"))
        assertNotNull(Validators.deadline("01/01/1999"))
    }

    @Test fun pauta_un_criterio_por_linea() {
        assertEquals(listOf("Uno", "Dos"), Validators.parseRubric("• Uno\n\n- Dos\n  "))
        assertNotNull(Validators.rubric("   \n  "))
        assertNull(Validators.rubric("Criterio claro\nOtro criterio"))
        assertNotNull(Validators.rubric((1..11).joinToString("\n") { "Criterio $it" }))
        assertNotNull(Validators.rubric("x".repeat(201)))
    }

    @Test fun pasos_y_preguntas_frecuentes() {
        assertNotNull(Validators.stepTitle("ab"))
        assertNull(Validators.stepTitle("Define el tema"))
        assertNotNull(Validators.stepContent("corto"))
        assertNull(Validators.stepContent("Elige un tema vinculado a tu quehacer."))
        assertNotNull(Validators.faqCategory(""))
        assertNull(Validators.faqCategory("Citación"))
        assertNotNull(Validators.faqQuestion("¿Cómo?"))
        assertNull(Validators.faqQuestion("¿Cómo cito un libro en APA 7?"))
        assertNotNull(Validators.faqAnswer("No"))
        assertNull(Validators.faqAnswer("Apellido, A. (año). Título. Editorial."))
    }

    @Test fun observaciones_obligatorias_solo_al_devolver() {
        assertNull(Validators.feedback("", returning = false))
        assertNotNull(Validators.feedback("", returning = true))
        assertNotNull(Validators.feedback("corto", returning = true))
        assertNull(Validators.feedback("Falta justificar el problema.", returning = true))
        assertNotNull(Validators.feedback("x".repeat(501), returning = false))
    }

    @Test fun el_estudiante_no_puede_elegir_aprobado() {
        assertFalse(DeliverableStatus.APROBADO.studentSelectable)
        assertTrue(DeliverableStatus.ENVIADO.studentSelectable)
        assertTrue(DeliverableStatus.EN_DESARROLLO.studentSelectable)
        assertTrue(DeliverableStatus.POR_INICIAR.studentSelectable)
    }

    @Test fun rol_desconocido_se_trata_como_estudiante() {
        assertEquals(UserRole.PROFESOR, UserRole.from("PROFESOR"))
        assertEquals(UserRole.ESTUDIANTE, UserRole.from("ESTUDIANTE"))
        assertEquals(UserRole.ESTUDIANTE, UserRole.from(null))
        assertEquals(UserRole.ESTUDIANTE, UserRole.from("ADMIN"))
    }
}
