package br.com.emanoel_raiff.carteira_digital_api.repository;

import br.com.emanoel_raiff.carteira_digital_api.entity.Usuario;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

@Repository
public interface UsuarioRepository extends JpaRepository<Usuario, Long> {

    Boolean existsByEmail(String email);

    Boolean existsByCpf(String cpf);

}