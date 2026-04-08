package br.com.emanoel_raiff.carteira_digital_api.service;

import br.com.emanoel_raiff.carteira_digital_api.entity.Transacao;
import br.com.emanoel_raiff.carteira_digital_api.entity.Usuario;
import br.com.emanoel_raiff.carteira_digital_api.exception.EntidadeNaoEncontradaException;
import br.com.emanoel_raiff.carteira_digital_api.repository.TransacaoRepository;
import br.com.emanoel_raiff.carteira_digital_api.repository.UsuarioRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.util.List;

@Service
public class TransacaoService {

    @Autowired
    private TransacaoRepository transacaoRepository;

    @Autowired
    private UsuarioRepository usuarioRepository;

    public Transacao salvar(Transacao transacao) {
        // 1. Pega o ID que o Postman enviou (ex: 4)
        Long usuarioId = transacao.getUsuario().getId();

        // 2. Vai no banco de dados e busca o usuário completo. Se não achar, já lança o erro 404!
        Usuario usuarioCompleto = usuarioRepository.findById(usuarioId)
                .orElseThrow(() -> new EntidadeNaoEncontradaException("Usuário não encontrado."));

        // 3. Cola o usuário completo (com nome, CPF, etc) na transação, substituindo aquele que só tinha o ID
        transacao.setUsuario(usuarioCompleto);

        // 4. Salva no banco e devolve a resposta bonita e completa
        return transacaoRepository.save(transacao);
    }

    public List<Transacao> listarTodas() {
        return transacaoRepository.findAll();
    }
}