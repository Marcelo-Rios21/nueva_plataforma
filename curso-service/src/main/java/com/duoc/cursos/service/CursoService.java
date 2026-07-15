package com.duoc.cursos.service;

import java.math.BigDecimal;
import java.util.List;

import org.springframework.stereotype.Service;

import com.duoc.cursos.exception.RecursoNoEncontradoException;
import com.duoc.cursos.model.Curso;
import com.duoc.cursos.repository.CursoRepository;

@Service
public class CursoService {

    private final CursoRepository cursoRepository;

    public CursoService(CursoRepository cursoRepository) {
        this.cursoRepository = cursoRepository;
    }

    public List<Curso> listarCursos() {
        return cursoRepository.findAll();
    }

    public Curso buscarCursoPorId(Long id) {
        return cursoRepository.findById(id)
                .orElseThrow(() ->
                        new RecursoNoEncontradoException(
                                "Curso no encontrado con ID: " + id
                        )
                );
    }

    public List<Curso> buscarCursosPorIds(List<Long> ids) {
        if (ids == null || ids.isEmpty()) {
            throw new IllegalArgumentException(
                    "Debe indicar al menos un ID de curso."
            );
        }

        return cursoRepository.findAllById(ids);
    }

    public Curso registrarCurso(Curso curso) {
        validarCurso(curso);
        return cursoRepository.save(curso);
    }

    public Curso actualizarCurso(Long id, Curso cursoActualizado) {
        Curso cursoExistente = buscarCursoPorId(id);

        validarCurso(cursoActualizado);

        cursoExistente.setNombre(cursoActualizado.getNombre());
        cursoExistente.setInstructor(cursoActualizado.getInstructor());
        cursoExistente.setDuracion(cursoActualizado.getDuracion());
        cursoExistente.setCosto(cursoActualizado.getCosto());

        return cursoRepository.save(cursoExistente);
    }

    public void eliminarCurso(Long id) {
        if (!cursoRepository.existsById(id)) {
            throw new RecursoNoEncontradoException(
                    "Curso no encontrado con ID: " + id
            );
        }

        cursoRepository.deleteById(id);
    }

    private void validarCurso(Curso curso) {
        if (curso == null) {
            throw new IllegalArgumentException(
                    "Los datos del curso son obligatorios."
            );
        }

        if (curso.getNombre() == null || curso.getNombre().isBlank()) {
            throw new IllegalArgumentException(
                    "El nombre del curso es obligatorio."
            );
        }

        if (curso.getInstructor() == null || curso.getInstructor().isBlank()) {
            throw new IllegalArgumentException(
                    "El instructor del curso es obligatorio."
            );
        }

        if (curso.getDuracion() == null || curso.getDuracion().isBlank()) {
            throw new IllegalArgumentException(
                    "La duración del curso es obligatoria."
            );
        }

        if (curso.getCosto() == null
                || curso.getCosto().compareTo(BigDecimal.ZERO) <= 0) {

            throw new IllegalArgumentException(
                    "El costo del curso debe ser mayor a cero."
            );
        }
    }
}