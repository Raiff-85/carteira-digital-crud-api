package br.com.emanoel_raiff.carteira_digital_api.service;

import br.com.emanoel_raiff.carteira_digital_api.entity.Usuario;
import br.com.emanoel_raiff.carteira_digital_api.exception.RegraDeNegocioException;
import br.com.emanoel_raiff.carteira_digital_api.repository.UsuarioRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.util.List;

@Service
public class UsuarioService {

    @Autowired
    private UsuarioRepository repository;

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