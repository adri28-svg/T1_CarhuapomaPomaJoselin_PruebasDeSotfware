import org.junit.jupiter.api.Test;
import static org.junit.jupiter.api.Assertions.*;
import java.time.LocalDateTime;

class ServicioCitasImplTest { // @ Joselin Carhuapoma

	// === DATOS PERSONALIZADOS ===
	private static final String PLACA = "CAR-234"; // Últimos 3 dígitos de la placa de Joselin
	private static final int DIA = 17; // Día calculado según su caso
	private final ServicioCitas servicio = new ServicioCitasImpl();

	// ==================== PREGUNTA 01: REGISTRO DE CITAS ====================
	@Test
	void registrarCita_CambioAceite_Correcto_GuardaYVerifica() {
		LocalDateTime fecha = LocalDateTime.of(2026, 9, DIA, 10, 0);
		Cita cita = new Cita(PLACA, TipoServicio.CAMBIO_ACEITE, fecha, DuracionServicio.CORTA);

		Cita resultado = servicio.registrarCita(cita);

		assertNotNull(resultado);
		assertEquals(EstadoCita.PROGRAMADA, resultado.getEstado());
		assertEquals(DuracionServicio.CORTA.getMinutos(), resultado.getDuracion());
		assertTrue(servicio.listarCitas().contains(resultado));
	}

	@Test
	void registrarCita_IdMecanico99_NoExiste_LanzaExcepcionSinGuardar() {
		LocalDateTime fecha = LocalDateTime.of(2026, 9, DIA, 10, 0);
		Cita cita = new Cita(PLACA, TipoServicio.CAMBIO_ACEITE, fecha, DuracionServicio.CORTA);
		cita.setIdMecanico(99L);

		assertThrows(MecanicoNoEncontradoException.class, () -> servicio.registrarCita(cita));
		assertTrue(servicio.listarCitas().isEmpty());
	}

	@Test
	void registrarCita_ReparacionMotor_EspecialidadIncorrecta_LanzaExcepcionSinGuardar() {
		LocalDateTime fecha = LocalDateTime.of(2026, 9, DIA, 10, 0);
		Cita cita = new Cita(PLACA, TipoServicio.REPARACION_MOTOR, fecha, DuracionServicio.LARGA);
		cita.setIdMecanico(1L); // Mecánico solo sabe CAMBIO_ACEITE

		assertThrows(EspecialidadIncorrectaException.class, () -> servicio.registrarCita(cita));
		assertTrue(servicio.listarCitas().isEmpty());
	}

	// ==================== PREGUNTA 02: HORARIO SERVICIOS PESADOS ====================
	@Test
	void horarioPesado_0700_NoPermitido_LanzaExcepcion() {
		LocalDateTime fecha = LocalDateTime.of(2026, 9, DIA, 7, 0);
		Cita cita = new Cita(PLACA, TipoServicio.REPARACION_MOTOR, fecha, DuracionServicio.LARGA);

		assertThrows(HorarioNoPermitidoException.class, () -> servicio.registrarCita(cita));
	}

	@Test
	void horarioPesado_0800_Permitido_RegistraCorrectamente() {
		LocalDateTime fecha = LocalDateTime.of(2026, 9, DIA, 8, 0);
		Cita cita = new Cita(PLACA, TipoServicio.REPARACION_MOTOR, fecha, DuracionServicio.LARGA);

		Cita resultado = servicio.registrarCita(cita);

		assertEquals(EstadoCita.PROGRAMADA, resultado.getEstado());
	}

	@Test
	void horarioPesado_1100_Permitido_RegistraCorrectamente() {
		LocalDateTime fecha = LocalDateTime.of(2026, 9, DIA, 11, 0);
		Cita cita = new Cita(PLACA, TipoServicio.REPARACION_MOTOR, fecha, DuracionServicio.LARGA);

		Cita resultado = servicio.registrarCita(cita);

		assertEquals(EstadoCita.PROGRAMADA, resultado.getEstado());
	}

	@Test
	void horarioPesado_1200_NoPermitido_LanzaExcepcion() {
		LocalDateTime fecha = LocalDateTime.of(2026, 9, DIA, 12, 0);
		Cita cita = new Cita(PLACA, TipoServicio.REPARACION_MOTOR, fecha, DuracionServicio.LARGA);

		assertThrows(HorarioNoPermitidoException.class, () -> servicio.registrarCita(cita));
	}

	// ==================== PREGUNTA 03: CANCELACIÓN DE CITAS ====================
	@Test
	void cancelarCita_24HorasAntes_SinPenalidad_NotificaCancelacion() {
		LocalDateTime fecha = LocalDateTime.of(2026, 9, DIA, 10, 0);
		Cita cita = new Cita(PLACA, TipoServicio.CAMBIO_ACEITE, fecha, DuracionServicio.CORTA);
		ServicioCitas servicioCancelar = new ServicioCitasImpl(() -> fecha.minusDays(1));
		Cita registrada = servicioCancelar.registrarCita(cita);

		Cita cancelada = servicioCancelar.cancelarCita(registrada.getId());

		assertEquals(EstadoCita.CANCELADA, cancelada.getEstado());
		assertEquals(PenalidadCita.NINGUNA, cancelada.getPenalidad());
		assertTrue(cancelada.getNotificacion().toLowerCase().contains("cancelada"));
	}

	@Test
	void cancelarCita_2HorasAntes_ConPenalidad() {
		LocalDateTime fecha = LocalDateTime.of(2026, 9, DIA, 10, 0);
		Cita cita = new Cita(PLACA, TipoServicio.CAMBIO_ACEITE, fecha, DuracionServicio.CORTA);
		Cita registrada = servicio.registrarCita(cita);
		ServicioCitas servicioCancelar = new ServicioCitasImpl(() -> fecha.minusHours(2));

		Cita cancelada = servicioCancelar.cancelarCita(registrada.getId());

		assertEquals(EstadoCita.CANCELADA, cancelada.getEstado());
		assertEquals(PenalidadCita.PARCIAL, cancelada.getPenalidad());
	}

	@Test
	void cancelarCitaYaAtendida_NoPermitido_LanzaExcepcion() {
		LocalDateTime fecha = LocalDateTime.of(2026, 9, DIA, 10, 0);
		Cita cita = new Cita(PLACA, TipoServicio.CAMBIO_ACEITE, fecha, DuracionServicio.CORTA);
		Cita registrada = servicio.registrarCita(cita);
		servicio.marcarComoAtendida(registrada.getId());

		assertThrows(CitaNoCancelableException.class, () -> servicio.cancelarCita(registrada.getId()));
	}
}
