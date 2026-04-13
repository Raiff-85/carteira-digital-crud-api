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

    private Usuario buscarUsuarioOuFalhar(Long id) {
        return usuarioRepository.findById(id)
                .orElseThrow(() -> new EntidadeNaoEncontradaException("Usuário não encontrado (ID: " + id + ")"));
    }

    public List<Transacao> listarTodas() {
        return transacaoRepository.findAll();
    }

    // Método salvar corrigido para a nova arquitetura
    public Transacao salvar(Transacao transacao) {
        // Se a transação estiver tentando ser salva sem carteira, lançamos erro
        if (transacao.getCarteira() == null || transacao.getCarteira().getId() == null) {
            throw new RegraDeNegocioException("Uma carteira válida deve ser informada.");
        }
        return transacaoRepository.save(transacao);
    }

    @Transactional
    public Transacao depositar(Long usuarioId, BigDecimal valor) {
        Usuario usuario = buscarUsuarioOuFalhar(usuarioId);

        BigDecimal novoSaldo = usuario.getCarteira().getSaldo().add(valor);
        usuario.getCarteira().setSaldo(novoSaldo);
        usuarioRepository.save(usuario);

        Transacao transacao = new Transacao();
        transacao.setValor(valor);
        transacao.setTipo(TipoTransacao.DEPOSITO);
        transacao.setCarteira(usuario.getCarteira());
        transacao.setDescricao("Depósito em conta");

        return transacaoRepository.save(transacao);
    }

    @Transactional
    public Transacao sacar(Long usuarioId, BigDecimal valor) {
        Usuario usuario = buscarUsuarioOuFalhar(usuarioId);

        if (usuario.getCarteira().getSaldo().compareTo(valor) < 0) {
            throw new RegraDeNegocioException("Saldo insuficiente para realizar o saque.");
        }

        BigDecimal novoSaldo = usuario.getCarteira().getSaldo().subtract(valor);
        usuario.getCarteira().setSaldo(novoSaldo);
        usuarioRepository.save(usuario);

        Transacao transacao = new Transacao();
        transacao.setValor(valor);
        transacao.setTipo(TipoTransacao.SAQUE);
        transacao.setCarteira(usuario.getCarteira());
        transacao.setDescricao("Saque em espécie");

        return transacaoRepository.save(transacao);
    }

    @Transactional
    public List<Transacao> transferir(Long usuarioIdOrigem, Long usuarioIdDestino, BigDecimal valor) {
        if (usuarioIdOrigem.equals(usuarioIdDestino)) {
            throw new RegraDeNegocioException("Não é possível realizar uma transferência para a própria conta.");
        }

        Usuario origem = buscarUsuarioOuFalhar(usuarioIdOrigem);
        Usuario destino = buscarUsuarioOuFalhar(usuarioIdDestino);

        if (origem.getCarteira().getSaldo().compareTo(valor) < 0) {
            throw new RegraDeNegocioException("Saldo insuficiente para transferência.");
        }

        origem.getCarteira().setSaldo(origem.getCarteira().getSaldo().subtract(valor));
        destino.getCarteira().setSaldo(destino.getCarteira().getSaldo().add(valor));

        usuarioRepository.save(origem);
        usuarioRepository.save(destino);

        Transacao saida = new Transacao();
        saida.setValor(valor);
        saida.setTipo(TipoTransacao.TRANSFERENCIA_ENVIADA);
        saida.setCarteira(origem.getCarteira());
        saida.setDescricao("Enviado para " + destino.getNome());

        Transacao entrada = new Transacao();
        entrada.setValor(valor);
        entrada.setTipo(TipoTransacao.TRANSFERENCIA_RECEBIDA);
        entrada.setCarteira(destino.getCarteira());
        entrada.setDescricao("Recebido de " + origem.getNome());

        transacaoRepository.save(saida);
        transacaoRepository.save(entrada);

        return List.of(saida, entrada);
    }
}