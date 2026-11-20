package com.tacticaldev.usuarios.controller;

import com.tacticaldev.usuarios.exception.ApiException;
import com.tacticaldev.usuarios.model.PuntoVenta;
import com.tacticaldev.usuarios.repository.PuntoVentaRepository;
import jakarta.validation.Valid;
import org.springframework.http.HttpStatus;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/api/puntos-venta")
public class PuntoVentaController {
    private final PuntoVentaRepository repo;

    public PuntoVentaController(PuntoVentaRepository repo) {
        this.repo = repo;
    }

    @GetMapping
    public List<PuntoVenta> listar() {
        return repo.findAll();
    }

    @GetMapping("/{id}")
    public PuntoVenta obtener(@PathVariable Long id) {
        return repo.findById(id).orElseThrow(() -> new ApiException(HttpStatus.NOT_FOUND, "Punto de venta no encontrado"));
    }

    @PostMapping
    @ResponseStatus(HttpStatus.CREATED)
    public PuntoVenta crear(@Valid @RequestBody PuntoVenta p) {
        if (repo.existsByNombre(p.getNombre())) {
            throw new ApiException(HttpStatus.CONFLICT, "Ya existe un punto de venta con ese nombre");
        }
        p.setId(null);
        return repo.save(p);
    }

    @PutMapping("/{id}")
    public PuntoVenta actualizar(@PathVariable Long id, @Valid @RequestBody PuntoVenta p) {
        PuntoVenta actual = obtener(id);
        actual.setNombre(p.getNombre());
        actual.setLatitud(p.getLatitud());
        actual.setLongitud(p.getLongitud());
        return repo.save(actual);
    }

    @DeleteMapping("/{id}")
    @ResponseStatus(HttpStatus.NO_CONTENT)
    public void eliminar(@PathVariable Long id) {
        repo.delete(obtener(id));
    }
}
