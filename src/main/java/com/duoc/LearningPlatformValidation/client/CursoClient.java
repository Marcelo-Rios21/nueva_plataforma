package com.duoc.LearningPlatformValidation.client;

import java.util.List;

import org.springframework.beans.factory.annotation.Value;
import org.springframework.core.ParameterizedTypeReference;
import org.springframework.http.MediaType;
import org.springframework.stereotype.Component;
import org.springframework.web.client.RestClient;
import org.springframework.web.client.RestClientException;
import org.springframework.web.client.RestClientResponseException;

import com.duoc.LearningPlatformValidation.dto.CursoResponse;
import com.duoc.LearningPlatformValidation.exception.ServicioCursosNoDisponibleException;

@Component
public class CursoClient {

    private final RestClient restClient;

    public CursoClient(
            RestClient.Builder builder,
            @Value("${services.cursos.url}") String baseUrl) {

        this.restClient = builder
                .baseUrl(baseUrl)
                .build();
    }

    public List<CursoResponse> buscarCursosPorIds(List<Long> ids) {
        try {
            List<CursoResponse> cursos = restClient
                    .post()
                    .uri("/api/cursos/buscar-por-ids")
                    .contentType(MediaType.APPLICATION_JSON)
                    .body(ids)
                    .retrieve()
                    .body(
                            new ParameterizedTypeReference<
                                    List<CursoResponse>
                            >() {
                            }
                    );

            return cursos == null ? List.of() : cursos;
        }
        catch (RestClientResponseException ex) {
            throw new ServicioCursosNoDisponibleException(
                    "El servicio de cursos respondió con HTTP "
                            + ex.getStatusCode().value() + ".",
                    ex
            );
        }
        catch (RestClientException ex) {
            throw new ServicioCursosNoDisponibleException(
                    "No fue posible comunicarse con el servicio de cursos.",
                    ex
            );
        }
    }
}