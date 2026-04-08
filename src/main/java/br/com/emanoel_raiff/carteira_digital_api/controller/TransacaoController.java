package br.com.emanoel_raiff.carteira_digital_api.controller;

import br.com.emanoel_raiff.carteira_digital_api.entity.Transacao;
import br.com.emanoel_raiff.carteira_digital_api.service.TransacaoService;
import jakarta.validation.Valid;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/transacoes")
public class TransacaoController {

    // Agora injetamos apenas o Service. Nada de Repositories no Controller!
    @Autowired
    private TransacaoService service;

    @PostMapping
    public ResponseEntity<Transacao> criar(@Valid @RequestBody Transacao transacao) {

        // 1. Repassa o objeto que veio no JSON direto para o Service.
        // O Service vai fazer aquele "if (!usuarioRepository.existsById...)" por debaixo dos panos.
        Transacao transacaoSalva = service.salvar(transacao);

        // 2. Se o Service não lançar nenhum erro, devolve o 201 Created.
        return ResponseEntity.status(HttpStatus.CREATED).body(transacaoSalva);
    }

    @GetMapping
    public List<Transacao> listar() {
        // Apenas repassa a chamada para o Service buscar a lista
        return service.listarTodas();
    }
}