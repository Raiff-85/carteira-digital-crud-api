package br.com.emanoel_raiff.carteira_digital_crud_api.controller;

import br.com.emanoel_raiff.carteira_digital_crud_api.entity.Transacao;
import br.com.emanoel_raiff.carteira_digital_crud_api.service.TransacaoService;
import jakarta.validation.Valid;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Positive;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.math.BigDecimal;
import java.util.List;

@RestController
@RequestMapping("/transacoes")
public class TransacaoController {

    @Autowired
    private TransacaoService service;

    @PostMapping
    public ResponseEntity<Transacao> criar(@Valid @RequestBody Transacao transacao) {

        Transacao transacaoSalva = service.salvar(transacao);

        return ResponseEntity.status(HttpStatus.CREATED).body(transacaoSalva);
    }

    @GetMapping
    public List<Transacao> listar() {

        return service.listarTodas();
    }

    public record DepositoRequest(@NotNull @Positive BigDecimal valor) {}

    @PostMapping("/deposito/{usuarioId}")
    public ResponseEntity<Transacao> realizarDeposito(@PathVariable Long usuarioId,
            @Valid @RequestBody DepositoRequest request) {

        Transacao transacaoSalva = service.depositar(usuarioId, request.valor());
        return ResponseEntity.status(HttpStatus.CREATED).body(transacaoSalva);
    }

    public record SaqueRequest(@NotNull @Positive BigDecimal valor) {}

    @PostMapping("/saque/{usuarioId}")
    public ResponseEntity<Transacao> realizarSaque(
            @PathVariable Long usuarioId,
            @Valid @RequestBody SaqueRequest request) {

        Transacao transacaoSalva = service.sacar(usuarioId, request.valor());
        return ResponseEntity.status(HttpStatus.CREATED).body(transacaoSalva);
    }

    // 1. Criamos o DTO (record) para receber os dados da transferência
    public record TransferenciaRequest(
            @NotNull(message = "O ID do usuário de origem é obrigatório.")
            Long usuarioIdOrigem,

            @NotNull(message = "O ID do usuário de destino é obrigatório.")
            Long usuarioIdDestino,

            @NotNull(message = "O valor da transferência é obrigatório.")
            @Positive(message = "O valor da transferência deve ser maior que zero.")
            BigDecimal valor
    ) {}

    @PostMapping("/transferencia")
    public ResponseEntity<List<Transacao>> realizarTransferencia(@Valid @RequestBody TransferenciaRequest request) {

        List<Transacao> transacoesSalvas = service.transferir(
                request.usuarioIdOrigem(),
                request.usuarioIdDestino(),
                request.valor()
        );

        return ResponseEntity.status(HttpStatus.CREATED).body(transacoesSalvas);
    }
}