package br.com.emanoel_raiff.carteira_digital_api.controller;

import br.com.emanoel_raiff.carteira_digital_api.entity.Usuario;
import br.com.emanoel_raiff.carteira_digital_api.service.UsuarioService;
import jakarta.validation.Valid;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/usuarios")
public class UsuarioController {

    @Autowired
    private UsuarioService service; // Injeta o Service, não o Repository

    @PostMapping
    public ResponseEntity<Usuario> criar(@Valid @RequestBody Usuario usuario) {
        // Chama o service para processar a criação
        Usuario usuarioSalvo = service.salvar(usuario);
        return ResponseEntity.status(HttpStatus.CREATED).body(usuarioSalvo);
    }

    @GetMapping
    public List<Usuario> listar() {
        return service.listarTodos();
    }
}