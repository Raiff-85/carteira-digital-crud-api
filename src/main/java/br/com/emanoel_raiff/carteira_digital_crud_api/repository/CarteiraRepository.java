package br.com.emanoel_raiff.carteira_digital_crud_api.repository;

import br.com.emanoel_raiff.carteira_digital_crud_api.entity.Carteira;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

@Repository
public interface CarteiraRepository extends JpaRepository<Carteira, Long> {
}