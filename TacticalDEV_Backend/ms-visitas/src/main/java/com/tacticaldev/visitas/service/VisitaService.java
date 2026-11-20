package com.tacticaldev.visitas.service;

import com.tacticaldev.visitas.dto.VisitaDto;
import com.tacticaldev.visitas.exception.ApiException;
import com.tacticaldev.visitas.model.Visita;
import com.tacticaldev.visitas.repository.VisitaRepository;
import org.springframework.data.domain.Sort;
import org.springframework.http.HttpStatus;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;
import java.util.Locale;

@Service
public class VisitaService {
    private final VisitaRepository repo;

    public VisitaService(VisitaRepository repo) {
        this.repo = repo;
    }

    @Transactional(readOnly = true)
    public List<VisitaDto> listar(String codigoActivo, String tienda) {
        String cod = normalizar(codigoActivo);
        String tnd = normalizar(tienda);
        return repo.findAll(Sort.by(Sort.Direction.DESC, "marcaTiempo")).stream()
                .filter(v -> cod.isEmpty() || v.getCodigoActivo().toLowerCase(Locale.ROOT).contains(cod))
                .filter(v -> tnd.isEmpty() || v.getNombreTienda().toLowerCase(Locale.ROOT).contains(tnd))
                .map(VisitaService::aDto)
                .toList();
    }

    @Transactional(readOnly = true)
    public VisitaDto obtener(Long id) {
        return aDto(buscar(id));
    }

    @Transactional
    public VisitaDto crear(VisitaDto dto) {
        validarReglaDeNegocio(dto);
        Visita v = new Visita();
        copiar(dto, v);
        v.setMarcaTiempo(dto.marcaTiempo() != null ? dto.marcaTiempo() : System.currentTimeMillis());
        return aDto(repo.save(v));
    }

    @Transactional
    public VisitaDto actualizar(Long id, VisitaDto dto) {
        validarReglaDeNegocio(dto);
        Visita v = buscar(id);
        copiar(dto, v);
        if (dto.marcaTiempo() != null) {
            v.setMarcaTiempo(dto.marcaTiempo());
        }
        return aDto(repo.save(v));
    }

    @Transactional
    public void eliminar(Long id) {
        repo.delete(buscar(id));
    }


    private Visita buscar(Long id) {
        return repo.findById(id).orElseThrow(() -> new ApiException(HttpStatus.NOT_FOUND, "La visita " + id + " no existe"));
    }

    private void validarReglaDeNegocio(VisitaDto dto) {
        if (!Boolean.TRUE.equals(dto.presenciaVerificada())) {
            throw new ApiException(HttpStatus.UNPROCESSABLE_ENTITY,
                    "La presencia física no está verificada. La visita no puede registrarse.");
        }
    }

    private static String normalizar(String s) {
        return s == null ? "" : s.trim().toLowerCase(Locale.ROOT);
    }

    private static void copiar(VisitaDto d, Visita v) {
        v.setCorreo(d.correo().trim().toLowerCase(Locale.ROOT));
        v.setNombreOperario(d.nombreOperario().trim());
        v.setRolOperario(d.rolOperario().trim());
        v.setCodigoActivo(d.codigoActivo().trim().toUpperCase(Locale.ROOT));
        v.setTipoActivo(d.tipoActivo());
        v.setNombreTienda(d.nombreTienda().trim());
        v.setTipoVisita(d.tipoVisita());
        v.setEstadoEquipo(d.estadoEquipo());
        v.setObservaciones(d.observaciones().trim());
        v.setRutaFoto(d.rutaFoto());
        v.setLatitud(d.latitud());
        v.setLongitud(d.longitud());
        v.setPrecisionGps(d.precisionGps());
        v.setDistanciaPuntoVentaMetros(d.distanciaPuntoVentaMetros());
        v.setPresenciaVerificada(d.presenciaVerificada());
    }

    static VisitaDto aDto(Visita v) {
        return new VisitaDto(v.getId(), v.getCorreo(), v.getNombreOperario(), v.getRolOperario(),
                v.getCodigoActivo(), v.getTipoActivo(), v.getNombreTienda(), v.getTipoVisita(),
                v.getEstadoEquipo(), v.getObservaciones(), v.getRutaFoto(), v.getLatitud(), v.getLongitud(),
                v.getPrecisionGps(), v.getDistanciaPuntoVentaMetros(), v.getPresenciaVerificada(), v.getMarcaTiempo());
    }
}
