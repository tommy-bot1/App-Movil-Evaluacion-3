package com.tacticaldev.visitas.controller;

import com.tacticaldev.visitas.dto.VisitaDto;
import com.tacticaldev.visitas.service.VisitaService;
import jakarta.validation.Valid;
import org.springframework.http.HttpStatus;
import org.springframework.web.bind.annotation.*;

import java.util.List;
import java.util.Map;

@RestController
@RequestMapping("/api")
public class VisitaController {
    private final VisitaService servicio;

    public VisitaController(VisitaService servicio) {
        this.servicio = servicio;
    }

    @GetMapping("/ping")
    public Map<String, String> ping() {
        return Map.of("servicio", "ms-visitas", "estado", "ok");
    }

    @GetMapping("/visitas")
    public List<VisitaDto> listar(@RequestParam(required = false) String codigoActivo,
                                  @RequestParam(required = false) String tienda) {
        return servicio.listar(codigoActivo, tienda);
    }

    @GetMapping("/visitas/{id}")
    public VisitaDto obtener(@PathVariable Long id) {
        return servicio.obtener(id);
    }

    @PostMapping("/visitas")
    @ResponseStatus(HttpStatus.CREATED)
    public VisitaDto crear(@Valid @RequestBody VisitaDto dto) {
        return servicio.crear(dto);
    }

    @PutMapping("/visitas/{id}")
    public VisitaDto actualizar(@PathVariable Long id, @Valid @RequestBody VisitaDto dto) {
        return servicio.actualizar(id, dto);
    }

    @DeleteMapping("/visitas/{id}")
    @ResponseStatus(HttpStatus.NO_CONTENT)
    public void eliminar(@PathVariable Long id) {
        servicio.eliminar(id);
    }
}
