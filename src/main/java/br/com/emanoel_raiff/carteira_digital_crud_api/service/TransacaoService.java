package br.com.emanoel_raiff.carteira_digital_crud_api.service;

import br.com.emanoel_raiff.carteira_digital_crud_api.entity.TipoTransacao;
import br.com.emanoel_raiff.carteira_digital_crud_api.entity.Transacao;
import br.com.emanoel_raiff.carteira_digital_crud_api.entity.Usuario;
import br.com.emanoel_raiff.carteira_digital_crud_api.exception.EntidadeNaoEncontradaException;
import br.com.emanoel_raiff.carteira_digital_crud_api.exception.RegraDeNegocioException;
import br.com.emanoel_raiff.carteira_digital_crud_api.repository.TransacaoRepository;
import br.com.emanoel_raiff.carteira_digital_crud_api.repository.UsuarioRepository;
import jakarta.transaction.Transactional;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.math.BigDecimal;
import java.util.List;

@Service
public class TransacaoService {

    @Autowired
    private TransacaoRepository transacaoRepository;

    @Autowired
    private UsuarioRepository usuarioRepository;

    public Transacao salvar(Transacao transacao) {

        Long usuarioId = transacao.getUsuario().getId();

        Usuario usuarioCompleto = usuarioRepository.findById(usuarioId)
                .orElseThrow(() -> new EntidadeNaoEncontradaException("Usuário não encontrado."));

        transacao.setUsuario(usuarioCompleto);

        return transacaoRepository.save(transacao);
    }

    public List<Transacao> listarTodas() {

        return transacaoRepository.findAll();

    }

    @Transactional
    public Transacao depositar(Long usuarioId, BigDecimal valor) {

        Usuario usuario = usuarioRepository.findById(usuarioId)
                .orElseThrow(() -> new EntidadeNaoEncontradaException("Usuário não encontrado para depósito."));

        BigDecimal novoSaldo = usuario.getSaldo().add(valor);
        usuario.setSaldo(novoSaldo);
        usuarioRepository.save(usuario);

        Transacao transacao = new Transacao();
        transacao.setValor(valor);
        transacao.setTipo(TipoTransacao.DEPOSITO);
        transacao.setUsuario(usuario);

        return transacaoRepository.save(transacao);
    }

    @Transactional
    public Transacao sacar(Long usuarioId, BigDecimal valor) {

        Usuario usuario = usuarioRepository.findById(usuarioId)
                .orElseThrow(() -> new EntidadeNaoEncontradaException("Usuário não encontrado para saque."));

        if (usuario.getSaldo().compareTo(valor) < 0) {
            throw new RegraDeNegocioException("Saldo insuficiente para realizar o saque.");
        }

        BigDecimal novoSaldo = usuario.getSaldo().subtract(valor);
        usuario.setSaldo(novoSaldo);
        usuarioRepository.save(usuario);

        Transacao transacao = new Transacao();
        transacao.setValor(valor);
        transacao.setTipo(TipoTransacao.SAQUE);
        transacao.setUsuario(usuario);

        return transacaoRepository.save(transacao);
    }

    @Transactional
    public List<Transacao> transferir(Long usuarioIdOrigem, Long usuarioIdDestino, BigDecimal valor) {

        // 1. Não transferir para si mesmo
        if (usuarioIdOrigem.equals(usuarioIdDestino)) {
            throw new RegraDeNegocioException("Não é possível realizar uma transferência para a própria conta.");
        }

        // 2. Busca dos usuários
        Usuario usuarioOrigem = usuarioRepository.findById(usuarioIdOrigem)
                .orElseThrow(() -> new EntidadeNaoEncontradaException("Usuário de origem não encontrado."));

        Usuario usuarioDestino = usuarioRepository.findById(usuarioIdDestino)
                .orElseThrow(() -> new EntidadeNaoEncontradaException("Usuário de destino não encontrado."));

        // 3. Validação de saldo da Origem
        if (usuarioOrigem.getSaldo().compareTo(valor) < 0) {
            throw new RegraDeNegocioException("Saldo insuficiente para realizar a transferência.");
        }

        // 4. Atualização matemática dos saldos
        usuarioOrigem.setSaldo(usuarioOrigem.getSaldo().subtract(valor));
        usuarioDestino.setSaldo(usuarioDestino.getSaldo().add(valor));

        usuarioRepository.save(usuarioOrigem);
        usuarioRepository.save(usuarioDestino);

        // ==========================================
        // 5. O LANÇAMENTO DUPLO COMEÇA AQUI
        // ==========================================

        // Lançamento 1: O extrato de quem ENVIou o dinheiro (Saiu saldo)
        Transacao transacaoSaida = new Transacao();
        transacaoSaida.setValor(valor);
        transacaoSaida.setTipo(TipoTransacao.TRANSFERENCIA_ENVIADA);
        transacaoSaida.setUsuario(usuarioOrigem);
        transacaoSaida.setDescricao("Transferência enviada para " + usuarioDestino.getNome());

        // Lançamento 2: O extrato de quem RECEBEU o dinheiro (Entrou saldo)
        Transacao transacaoEntrada = new Transacao();
        transacaoEntrada.setValor(valor);
        transacaoEntrada.setTipo(TipoTransacao.TRANSFERENCIA_RECEBIDA);
        transacaoEntrada.setUsuario(usuarioDestino);
        transacaoEntrada.setDescricao("Transferência recebida de " + usuarioOrigem.getNome());

        // Salva as duas no banco de dados!
        transacaoRepository.save(transacaoSaida);
        transacaoRepository.save(transacaoEntrada);

        // Como criamos duas transações, vamos retornar uma Lista com ambas
        // para o Controller saber o que aconteceu
        return List.of(transacaoSaida, transacaoEntrada);
    }
}