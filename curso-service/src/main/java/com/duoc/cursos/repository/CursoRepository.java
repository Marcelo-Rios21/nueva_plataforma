package com.duoc.cursos.repository;

import org.springframework.data.jpa.repository.JpaRepository;

import com.duoc.cursos.model.Curso;

public interface CursoRepository extends JpaRepository<Curso, Long> {
}