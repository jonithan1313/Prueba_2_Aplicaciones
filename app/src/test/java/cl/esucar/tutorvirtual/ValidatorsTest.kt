package cl.esucar.tutorvirtual

import cl.esucar.tutorvirtual.domain.DeliverableStatus
import cl.esucar.tutorvirtual.domain.Validators
import org.junit.Assert.assertEquals
import org.junit.Assert.assertNotNull
import org.junit.Assert.assertNull
import java.time.LocalDate
import org.junit.Test

class ValidatorsTest {
    @Test fun email_valido_e_invalido() {
        assertNull(Validators.email("ana@correo.cl"))
        assertNotNull(Validators.email("ana@correo"))
        assertNotNull(Validators.email(""))
    }

    @Test fun password_exige_largo_letra_y_numero() {
        assertNotNull(Validators.password("abc123"))
        assertNotNull(Validators.password("abcdefgh"))
        assertNotNull(Validators.password("12345678"))
        assertNull(Validators.password("abcd1234"))
    }

    @Test fun confirmacion_debe_coincidir() {
        assertNull(Validators.confirmPassword("abcd1234", "abcd1234"))
        assertNotNull(Validators.confirmPassword("abcd1234", "abcd12345"))
    }

    @Test fun fecha_de_entrega_obligatoria_si_esta_enviado_y_no_futura() {
        val today = LocalDate.of(2026, 11, 12)
        assertNotNull(Validators.sentDate("", DeliverableStatus.ENVIADO, today))
        assertNull(Validators.sentDate("", DeliverableStatus.EN_DESARROLLO, today))
        assertNotNull(Validators.sentDate("31/02/2026", DeliverableStatus.ENVIADO, today))
        assertNotNull(Validators.sentDate("13/11/2026", DeliverableStatus.ENVIADO, today))
        assertNull(Validators.sentDate("12/11/2026", DeliverableStatus.ENVIADO, today))
    }

    @Test fun notas_obligatorias_al_enviar() {
        assertNotNull(Validators.notes("corto", DeliverableStatus.ENVIADO))
        assertNull(Validators.notes("Entregué el capítulo completo", DeliverableStatus.ENVIADO))
        assertNull(Validators.notes("", DeliverableStatus.EN_DESARROLLO))
    }

    @Test fun dias_de_recordatorio() {
        assertNull(Validators.reminderDays("3,1"))
        assertNotNull(Validators.reminderDays("3,3"))
        assertNotNull(Validators.reminderDays("40"))
        assertNotNull(Validators.reminderDays("a,b"))
        assertNotNull(Validators.reminderDays("1,2,3,4"))
        assertEquals(listOf(3, 1), Validators.parseReminderDays("1, 3"))
    }
}
