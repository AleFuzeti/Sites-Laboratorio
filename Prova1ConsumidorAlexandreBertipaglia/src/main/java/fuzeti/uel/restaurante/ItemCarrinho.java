package fuzeti.uel.restaurante;

public class ItemCarrinho extends Item {
    private int quantidade;

    public ItemCarrinho(Item item, int quantidade) {
        this.setId(item.getId());
        this.setNome(item.getNome());
        this.setDescricao(item.getDescricao());
        this.setPreco(item.getPreco());
        this.setRestaurante(item.getRestaurante());
        this.quantidade = quantidade;
    }

    public int getQuantidade() {
        return quantidade;
    }

    public void setQuantidade(int quantidade) {
        this.quantidade = quantidade;
    }
}
