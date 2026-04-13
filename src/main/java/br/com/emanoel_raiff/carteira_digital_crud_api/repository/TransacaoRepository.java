package br.com.emanoel_raiff.carteira_digital_crud_api.repository;

import br.com.emanoel_raiff.carteira_digital_crud_api.entity.Transacao;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

@Repository
public interface TransacaoRepository extends JpaRepository<Transacao, Long> {
}