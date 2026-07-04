package com.duoc.LearningPlatformValidation.repository;

import com.duoc.LearningPlatformValidation.model.ResumenCompraMq;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.Optional;

public interface ResumenCompraMqRepository extends JpaRepository<ResumenCompraMq, Long> {

    Optional<ResumenCompraMq> findByInscripcionId(Long inscripcionId);
}