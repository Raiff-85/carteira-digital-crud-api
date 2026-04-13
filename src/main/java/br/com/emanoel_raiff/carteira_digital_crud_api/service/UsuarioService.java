package br.com.emanoel_raiff.carteira_digital_crud_api.service;

import br.com.emanoel_raiff.carteira_digital_crud_api.entity.Usuario;
import br.com.emanoel_raiff.carteira_digital_crud_api.exception.EntidadeNaoEncontradaException;
import br.com.emanoel_raiff.carteira_digital_crud_api.exception.RegraDeNegocioException;
import br.com.emanoel_raiff.carteira_digital_crud_api.repository.UsuarioRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.math.BigDecimal;
import java.util.List;

@Service
public class UsuarioService {

    @Autowired
    private UsuarioRepository repository;

    public Usuario buscarPorId(Long id) {
        return repository.findById(id)
                .orElseThrow(() -> new EntidadeNaoEncontradaException("Usuário não encontrado."));
    }

    public Usuario atualizar(Long id, Usuario dadosAtualizados) {
        Usuario usuarioExistente = buscarPorId(id);

        // Verificações de regra de negócio (garantir que não vai roubar o email de outro)
        if (!usuarioExistente.getEmail().equals(dadosAtualizados.getEmail()) && repository.existsByEmail(dadosAtualizados.getEmail())) {
            throw new RegraDeNegocioException("Este e-mail já está em uso por outro usuário.");
        }

        // Atualizamos apenas campos permitidos. NUNCA atualizamos o CPF, Saldo ou ID aqui.
        usuarioExistente.setNome(dadosAtualizados.getNome());
        usuarioExistente.setEmail(dadosAtualizados.getEmail());

        // Se a senha vier na requisição, nós atualizamos
        if (dadosAtualizados.getSenha() != null && !dadosAtualizados.getSenha().isBlank()) {
            usuarioExistente.setSenha(dadosAtualizados.getSenha());
        }

        return repository.save(usuarioExistente);
    }

    public void desativar(Long id) {
        Usuario usuarioExistente = buscarPorId(id);

        if (usuarioExistente.getSaldo().compareTo(BigDecimal.ZERO) > 0) {
            throw new RegraDeNegocioException("Não é possível excluir uma conta com saldo positivo.");
        }

        usuarioExistente.setAtivo(false);
        repository.save(usuarioExistente);
    }

    public Usuario salvar(Usuario usuario) {

        boolean cpfJaExiste = repository.existsByCpf(usuario.getCpf());
        boolean emailJaExiste = repository.existsByEmail(usuario.getEmail());

        if (cpfJaExiste && emailJaExiste) {
            throw new RegraDeNegocioException("CPF e E-mail já estão cadastrados no sistema.");
        } else if (cpfJaExiste) {
            throw new RegraDeNegocioException("CPF já cadastrado no sistema.");
        } else if (emailJaExiste) {
            throw new RegraDeNegocioException("E-mail já cadastrado no sistema.");
        }

        return repository.save(usuario);
    }

    public List<Usuario> listarTodos() {
        return repository.findAll();
    }
}