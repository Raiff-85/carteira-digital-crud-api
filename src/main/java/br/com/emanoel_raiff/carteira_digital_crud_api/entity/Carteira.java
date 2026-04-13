package br.com.emanoel_raiff.carteira_digital_crud_api.entity;

import com.fasterxml.jackson.annotation.JsonIgnore;
import jakarta.persistence.*;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.PositiveOrZero;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.math.BigDecimal;

@Entity
@Table(name = "carteiras")
@Data
@NoArgsConstructor
@AllArgsConstructor
public class Carteira {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @NotNull
    @PositiveOrZero(message = "O saldo não pode ser negativo.")
    @Column(nullable = false)
    private BigDecimal saldo = BigDecimal.ZERO;

    @OneToOne
    @JoinColumn(name = "usuario_id", nullable = false, unique = true)
    @JsonIgnore // Importante para evitar recursividade infinita no JSON
    private Usuario usuario;

    // Construtor auxiliar para facilitar a criação no Service
    public Carteira(Usuario usuario) {
        this.usuario = usuario;
        this.saldo = BigDecimal.ZERO;
    }
}
