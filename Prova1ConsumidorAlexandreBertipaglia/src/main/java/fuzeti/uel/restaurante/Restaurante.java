package fuzeti.uel.restaurante;

import jakarta.persistence.Entity;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import jakarta.persistence.OneToMany;
import jakarta.validation.constraints.NotBlank;

import java.io.Serializable;
import java.util.Set;

@Entity
public class Restaurante implements Serializable {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private int id;
    private String nome;

    @OneToMany(mappedBy = "restaurante")
    private Set<Item> itens;
    
    @Override
    public boolean equals(Object o) {
        if (o == null || this.getClass() != o.getClass()) {
            return false;
        }
        return ((Restaurante) o).id == (this.id);
    }

    @Override
    public int hashCode() {
        return id * 12345;
    }

    public int getId() {
        return id;
    }

    public String getNome() {
        return nome;
    }

    public Set<Item> getItens() {
        return itens;
    }
    
}
