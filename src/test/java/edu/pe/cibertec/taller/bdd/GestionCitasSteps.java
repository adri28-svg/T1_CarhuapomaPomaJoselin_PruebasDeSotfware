package edu.pe.cibertec.taller.bdd;

import static org.mockito.Mockito.*;

import edu.pe.cibertec.taller.modelo.Cita;
import edu.pe.cibertec.taller.modelo.TipoServicio;
import edu.pe.cibertec.taller.modelo.DuracionServicio;
import edu.pe.cibertec.taller.modelo.EstadoCita;
import edu.pe.cibertec.taller.repositorio.RepositorioCitas;
import edu.pe.cibertec.taller.repositorio.RepositorioMecanicos;
import edu.pe.cibertec.taller.servicio.impl.ServicioCitasImpl;
import edu.pe.cibertec.taller.util.ProveedorFechaHora;
import edu.pe.cibertec.taller.util.ServicioNotificaciones;
import io.cucumber.java.Before;
import io.cucumber.java.en.*;

import java.time.LocalDateTime;

import static org.junit.jupiter.api.Assertions.*;

public class GestionCitasSteps {

	private RepositorioMecanicos repositorioMecanicos;
	private RepositorioCitas repositorioCitas;
	private ProveedorFechaHora proveedorFechaHora;
	private ServicioNotificaciones servicioNotificaciones;
	private ServicioCitasImpl servicioCitas;
	private Cita cita;
	private Cita resultado;

	@Before
	public void inicializar() {
		repositorioMecanicos = mock(RepositorioMecanicos.class);
		repositorioCitas = mock(RepositorioCitas.class);
		proveedorFechaHora = mock(ProveedorFechaHora.class);
		servicioNotificaciones = mock(ServicioNotificaciones.class);
		servicioCitas = new ServicioCitasImpl(repositorioMecanicos, repositorioCitas,
				proveedorFechaHora, servicioNotificaciones);
	}

	// === Escenario: Registrar una cita correctamente ===
	@Given("una cita de cambio de aceite para el vehículo con placa {string}")
	public void unaCitaDeCambioDeAceite(String placa) {
		LocalDateTime fecha = LocalDateTime.of(2026, 9, 17, 10, 0);
		cita = new Cita(placa, TipoServicio.CAMBIO_ACEITE, fecha, DuracionServicio.CORTA);
	}

	@When("registro la cita en el sistema")
	public void registroLaCitaEnElSistema() {
		resultado = servicioCitas.registrarCita(cita);
	}

	@Then("la cita queda programada correctamente")
	public void laCitaQuedaProgramadaCorrectamente() {
		assertNotNull(resultado);
		assertEquals(EstadoCita.PROGRAMADA, resultado.getEstado());
	}

	// === Escenario: Cancelar una cita ===
	@Given("una cita registrada para el vehículo con placa {string}")
	public void unaCitaRegistrada(String placa) {
		LocalDateTime fecha = LocalDateTime.of(2026, 9, 17, 10, 0);
		cita = new Cita(placa, TipoServicio.CAMBIO_ACEITE, fecha, DuracionServicio.CORTA);
		resultado = servicioCitas.registrarCita(cita);
	}

	@When("cancelo la cita")
	public void canceloLaCita() {
		resultado = servicioCitas.cancelarCita(resultado.getId());
	}

	@Then("la cita queda cancelada")
	public void laCitaQuedaCancelada() {
		assertEquals(EstadoCita.CANCELADA, resultado.getEstado());
	}
}
