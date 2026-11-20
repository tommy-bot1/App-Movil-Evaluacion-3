package com.tacticaldev.usuarios.dto;

import com.tacticaldev.usuarios.model.Usuario;


public record UsuarioRespuesta(Long id, String correo, String nombre, String rol) {
    public static UsuarioRespuesta de(Usuario u) {
        return new UsuarioRespuesta(u.getId(), u.getCorreo(), u.getNombre(), u.getRol());
    }
}
