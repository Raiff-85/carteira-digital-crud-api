package br.com.emanoel_raiff.carteira_digital_crud_api.service;

import br.com.emanoel_raiff.carteira_digital_crud_api.entity.TipoTransacao;
import br.com.emanoel_raiff.carteira_digital_crud_api.entity.Transacao;
import br.com.emanoel_raiff.carteira_digital_crud_api.entity.Usuario;
import br.com.emanoel_raiff.carteira_digital_crud_api.exception.EntidadeNaoEncontradaException;
import br.com.emanoel_raiff.carteira_digital_crud_api.exception.RegraDeNegocioException;
import br.com.emanoel_raiff.carteira_digital_crud_api.repository.TransacaoRepository;
import br.com.emanoel_raiff.carteira_digital_crud_api.repository.UsuarioRepository;
import br.com.emanoel_raiff.carteira_digital_crud_api.repository.CarteiraRepository;
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

    @Autowired
    private CarteiraRepository carteiraRepository;

    public Transacao salvar(Transacao transacao) {
        // Validação preventiva: verifica se o ID da carteira foi enviado
        if (transacao.getCarteira() == null || transacao.getCarteira().getId() == null) {
            throw new RegraDeNegocioException("O ID da carteira é obrigatório para realizar uma transação.");
        }

        // Validação: verifica se a carteira realmente existe no banco
        carteiraRepository.findById(transacao.getCarteira().getId())
                .orElseThrow(() -> new EntidadeNaoEncontradaException("Carteira não encontrada para o ID informado."));

        return transacaoRepository.save(transacao);
    }

    public List<Transacao> listarTodas() {
        return transacaoRepository.findAll();
    }

    private Usuario buscarUsuarioOuFalhar(Long id) {
        return usuarioRepository.findById(id)
                .orElseThrow(() -> new EntidadeNaoEncontradaException("Usuário não encontrado."));
    }

    @Transactional
    public Transacao depositar(Long usuarioId, BigDecimal valor) {
        Usuario usuario = buscarUsuarioOuFalhar(usuarioId);
        usuario.getCarteira().setSaldo(usuario.getCarteira().getSaldo().add(valor));
        usuarioRepository.save(usuario);

        Transacao transacao = new Transacao();
        transacao.setValor(valor);
        transacao.setTipo(TipoTransacao.DEPOSITO);
        transacao.setCarteira(usuario.getCarteira());
        transacao.setDescricao("Depósito via API");

        return transacaoRepository.save(transacao);
    }

    @Transactional
    public Transacao sacar(Long usuarioId, BigDecimal valor) {
        Usuario usuario = buscarUsuarioOuFalhar(usuarioId);

        if (usuario.getCarteira().getSaldo().compareTo(valor) < 0) {
            throw new RegraDeNegocioException("Saldo insuficiente para saque.");
        }

        usuario.getCarteira().setSaldo(usuario.getCarteira().getSaldo().subtract(valor));
        usuarioRepository.save(usuario);

        Transacao transacao = new Transacao();
        transacao.setValor(valor);
        transacao.setTipo(TipoTransacao.SAQUE);
        transacao.setCarteira(usuario.getCarteira());
        transacao.setDescricao("Saque via API");

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

        return transacaoRepository.saveAll(List.of(saida, entrada));
    }
}