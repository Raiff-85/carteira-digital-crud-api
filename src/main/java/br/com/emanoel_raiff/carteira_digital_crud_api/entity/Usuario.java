package br.com.emanoel_raiff.carteira_digital_crud_api.entity;

import com.fasterxml.jackson.annotation.JsonProperty;
import jakarta.persistence.*;
import jakarta.validation.constraints.Email;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Size;
import org.hibernate.validator.constraints.br.CPF; // Olha que legal, o Java tem validação de CPF nativa!
import lombok.*;

import java.math.BigDecimal;

@Entity
@Table(name = "usuarios")
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@EqualsAndHashCode(of = "id")
public class Usuario {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @NotBlank(message = "O nome é obrigatório e não pode ficar em branco.")
    @Column(nullable = false, length = 100)
    private String nome;

    @NotBlank(message = "O e-mail é obrigatório.")
    @Email(message = "Formato de e-mail inválido.")
    @Column(nullable = false, unique = true, length = 100)
    private String email;

    @NotBlank(message = "O CPF é obrigatório.")
    @CPF(message = "CPF inválido.")
    @Column(nullable = false, unique = true, length = 14)
    private String cpf;

    @NotBlank(message = "A senha é obrigatória.")
    @JsonProperty(access = JsonProperty.Access.WRITE_ONLY)
    @Size(min = 6, message = "A senha deve ter no mínimo 6 caracteres.")
    @Column(nullable = false)
    private String senha;

    @Column(nullable = false)
    private BigDecimal saldo = BigDecimal.ZERO;

    @Column(nullable = false)
    private Boolean ativo = true; // Todo usuário nasce ativo

    @PrePersist
    public void prePersist() {
        if (this.ativo == null) {
            this.ativo = true;
        }

        if (this.saldo == null) {
            this.saldo = BigDecimal.ZERO;
        }
    }
}