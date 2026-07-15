package com.duoc.LearningPlatformValidation.service;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.util.List;
import java.util.Map;
import java.util.function.Function;
import java.util.stream.Collectors;

import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import com.duoc.LearningPlatformValidation.client.CursoClient;
import com.duoc.LearningPlatformValidation.dto.BoletaResponse;
import com.duoc.LearningPlatformValidation.dto.CursoInscritoResponse;
import com.duoc.LearningPlatformValidation.dto.CursoResponse;
import com.duoc.LearningPlatformValidation.dto.InscripcionRequest;
import com.duoc.LearningPlatformValidation.dto.InscripcionResumenResponse;
import com.duoc.LearningPlatformValidation.exception.RecursoNoEncontradoException;
import com.duoc.LearningPlatformValidation.model.Inscripcion;
import com.duoc.LearningPlatformValidation.model.InscripcionCurso;
import com.duoc.LearningPlatformValidation.model.Pago;
import com.duoc.LearningPlatformValidation.repository.InscripcionCursoRepository;
import com.duoc.LearningPlatformValidation.repository.InscripcionRepository;

@Service
public class InscripcionService {

    private final InscripcionRepository inscripcionRepository;
    private final InscripcionCursoRepository inscripcionCursoRepository;
    private final CursoClient cursoClient;
    private final PagoService pagoService;

    public InscripcionService(
            InscripcionRepository inscripcionRepository,
            InscripcionCursoRepository inscripcionCursoRepository,
            CursoClient cursoClient,
            PagoService pagoService) {

        this.inscripcionRepository = inscripcionRepository;
        this.inscripcionCursoRepository = inscripcionCursoRepository;
        this.cursoClient = cursoClient;
        this.pagoService = pagoService;
    }

    public List<Inscripcion> listarInscripciones() {
        return inscripcionRepository.findAll();
    }

    public Inscripcion buscarInscripcionPorId(Long id) {
        return inscripcionRepository.findById(id)
                .orElseThrow(() ->
                        new RecursoNoEncontradoException(
                                "Inscripción no encontrada con ID: " + id
                        )
                );
    }

    public List<Inscripcion> buscarInscripcionesPorEstudiante(
            Long estudianteId) {

        return inscripcionRepository.findByEstudianteId(estudianteId);
    }

    @Transactional
    public InscripcionResumenResponse registrarInscripcion(
            InscripcionRequest request) {

        validarRequest(request);

        List<CursoResponse> cursos =
                cursoClient.buscarCursosPorIds(request.getCursoIds());

        if (cursos.size() != request.getCursoIds().size()) {
            throw new RecursoNoEncontradoException(
                    "Uno o más cursos seleccionados no existen."
            );
        }

        Map<Long, CursoResponse> cursosPorId = cursos.stream()
                .collect(
                        Collectors.toMap(
                                CursoResponse::getId,
                                Function.identity()
                        )
                );

        List<CursoResponse> cursosOrdenados = request.getCursoIds()
                .stream()
                .map(cursosPorId::get)
                .toList();

        if (cursosOrdenados.stream().anyMatch(curso -> curso == null)) {
            throw new RecursoNoEncontradoException(
                    "Uno o más cursos seleccionados no existen."
            );
        }

        BigDecimal total = cursosOrdenados.stream()
                .map(CursoResponse::getCosto)
                .reduce(BigDecimal.ZERO, BigDecimal::add);

        Inscripcion inscripcion = new Inscripcion();
        inscripcion.setEstudianteId(request.getEstudianteId());
        inscripcion.setFechaInscripcion(LocalDate.now());
        inscripcion.setTotal(total);
        inscripcion.setMetodoPago(request.getMetodoPago());
        inscripcion.setEstadoPago(
                PagoService.ESTADO_APROBADO_SIMULADO
        );

        Inscripcion inscripcionGuardada =
                inscripcionRepository.save(inscripcion);

        List<InscripcionCurso> detalleCursos = cursosOrdenados.stream()
                .map(curso ->
                        new InscripcionCurso(
                                null,
                                inscripcionGuardada.getId(),
                                curso.getId(),
                                curso.getNombre(),
                                curso.getCosto()
                        )
                )
                .toList();

        inscripcionCursoRepository.saveAll(detalleCursos);

        Pago pago = new Pago();
        pago.setInscripcionId(inscripcionGuardada.getId());
        pago.setMonto(total);
        pago.setMetodoPago(request.getMetodoPago());
        pago.setEstado(PagoService.ESTADO_APROBADO_SIMULADO);

        pagoService.registrarPago(pago);

        return construirResumen(
                inscripcionGuardada,
                detalleCursos
        );
    }

    public BoletaResponse generarBoleta(Long inscripcionId) {
        Inscripcion inscripcion =
                buscarInscripcionPorId(inscripcionId);

        List<InscripcionCurso> detalleCursos =
                inscripcionCursoRepository.findByInscripcionId(
                        inscripcionId
                );

        List<CursoInscritoResponse> cursos = detalleCursos.stream()
                .map(detalle ->
                        new CursoInscritoResponse(
                                detalle.getCursoId(),
                                detalle.getNombreCurso(),
                                detalle.getCostoCurso()
                        )
                )
                .toList();

        String numeroBoleta =
                "BOL-" + String.format("%05d", inscripcion.getId());

        return new BoletaResponse(
                numeroBoleta,
                inscripcion.getId(),
                inscripcion.getEstudianteId(),
                inscripcion.getFechaInscripcion(),
                cursos,
                inscripcion.getTotal(),
                inscripcion.getMetodoPago(),
                inscripcion.getEstadoPago()
        );
    }

    public void eliminarInscripcion(Long id) {
        if (!inscripcionRepository.existsById(id)) {
            throw new RecursoNoEncontradoException(
                    "Inscripción no encontrada con ID: " + id
            );
        }

        inscripcionRepository.deleteById(id);
    }

    private InscripcionResumenResponse construirResumen(
            Inscripcion inscripcion,
            List<InscripcionCurso> detalleCursos) {

        List<CursoInscritoResponse> cursos = detalleCursos.stream()
                .map(detalle ->
                        new CursoInscritoResponse(
                                detalle.getCursoId(),
                                detalle.getNombreCurso(),
                                detalle.getCostoCurso()
                        )
                )
                .toList();

        return new InscripcionResumenResponse(
                inscripcion.getId(),
                inscripcion.getEstudianteId(),
                inscripcion.getFechaInscripcion(),
                cursos,
                inscripcion.getTotal(),
                inscripcion.getMetodoPago(),
                inscripcion.getEstadoPago()
        );
    }

    private void validarRequest(InscripcionRequest request) {
        if (request == null) {
            throw new IllegalArgumentException(
                    "Los datos de la inscripción son obligatorios."
            );
        }

        if (request.getEstudianteId() == null) {
            throw new IllegalArgumentException(
                    "El estudiante es obligatorio."
            );
        }

        if (request.getCursoIds() == null
                || request.getCursoIds().isEmpty()) {

            throw new IllegalArgumentException(
                    "Debe seleccionar al menos un curso."
            );
        }

        if (request.getMetodoPago() == null
                || request.getMetodoPago().isBlank()) {

            throw new IllegalArgumentException(
                    "El método de pago es obligatorio."
            );
        }
    }
}