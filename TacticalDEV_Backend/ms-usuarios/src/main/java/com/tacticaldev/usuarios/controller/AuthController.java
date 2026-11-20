package com.tacticaldev.usuarios.controller;

import com.tacticaldev.usuarios.dto.LoginRequest;
import com.tacticaldev.usuarios.dto.RegistroRequest;
import com.tacticaldev.usuarios.dto.UsuarioRespuesta;
import com.tacticaldev.usuarios.service.AuthService;
import jakarta.validation.Valid;
import org.springframework.http.HttpStatus;
import org.springframework.web.bind.annotation.*;

import java.util.Map;

@RestController
@RequestMapping("/api")
public class AuthController {
    private final AuthService servicio;

    public AuthController(AuthService servicio) {
        this.servicio = servicio;
    }


    @GetMapping("/ping")
    public Map<String, String> ping() {
        return Map.of("servicio", "ms-usuarios", "estado", "ok");
    }

    @PostMapping("/auth/login")
    public UsuarioRespuesta login(@Valid @RequestBody LoginRequest req) {
        return servicio.login(req);
    }

    @PostMapping("/usuarios")
    @ResponseStatus(HttpStatus.CREATED)
    public UsuarioRespuesta registrar(@Valid @RequestBody RegistroRequest req) {
        return servicio.registrar(req);
    }
}
