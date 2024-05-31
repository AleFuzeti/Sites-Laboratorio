package fuzeti.uel.restaurante;

import jakarta.servlet.http.HttpServletRequest;

import java.util.ArrayList;
import java.util.List;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.servlet.mvc.support.RedirectAttributes;

@Controller
public class RestauranteController {
    private static final String SESSION_CARRINHO = "sessionCarrinho";
    @Autowired
    RestauranteRepository restauranteRepository;
    @Autowired
    ItemRepository itemRepository;

    @GetMapping(value = { "/index", "/" })
    public String mostrarListaRestaurantes(Model model) {
        model.addAttribute("restaurantes", restauranteRepository.findAll());
        return "index";
    }

    @GetMapping("/cardapio/{id}")
    public String mostrarCardapio(@PathVariable("id") int id, Model model) {
        if (restauranteRepository.findById(id).isEmpty()) {
            return "redirect:/index";
        }
        Restaurante restaurante = restauranteRepository.findById(id).get();
        List<Item> itensCardapio = new ArrayList<>(restaurante.getItens());
        model.addAttribute("restaurante", restaurante);
        model.addAttribute("item_cardapio", itensCardapio);
        return "cardapio";
    }

    @PostMapping("/adicionar-ao-carrinho")
    public String adicionarItemAoCarrinho(@RequestParam("id") int id, HttpServletRequest request, RedirectAttributes redirectAttributes) {
        Item item = itemRepository.findById(id)
                .orElseThrow(() -> new IllegalArgumentException("O id do produto é inválido: " + id));
    
        List<ItemCarrinho> carrinho = (List<ItemCarrinho>) request.getSession().getAttribute(SESSION_CARRINHO);
        if (carrinho == null) {
            carrinho = new ArrayList<>();
        }
    
        // Verifica se o item já está no carrinho
        boolean itemJaNoCarrinho = false;
        for (ItemCarrinho i : carrinho) {
            if (i.getId() == item.getId()) {
                i.setQuantidade(i.getQuantidade() + 1); // Incrementa a quantidade
                itemJaNoCarrinho = true;
                break;
            }
        }
    
        // Se o item não estava no carrinho, adiciona-o
        if (!itemJaNoCarrinho) {
            ItemCarrinho itemCarrinho = new ItemCarrinho(item, 1);
            carrinho.add(itemCarrinho); // Adiciona o item ao carrinho
        }
    
        request.getSession().setAttribute(SESSION_CARRINHO, carrinho);
        return "redirect:/carrinho";
    }

    @GetMapping("/carrinho")
    public String mostrarCarrinho(Model model, HttpServletRequest request) {
        List<ItemCarrinho> carrinho = (List<ItemCarrinho>) request.getSession().getAttribute(SESSION_CARRINHO);
        List<ItemCarrinho> carrinhoAtualizado = carrinho != null ? new ArrayList<>(carrinho) : new ArrayList<>();
        float total = 0;

        // Verifica se os itens do carrinho ainda existem no banco de dados 
        if (carrinho != null) {
            for (ItemCarrinho item : carrinho) {
                if (itemRepository.findById(item.getId()).isEmpty()) {
                    carrinhoAtualizado.remove(item);
                } else { // atualiza o item com os dados do banco
                    item.setPreco(itemRepository.findById(item.getId()).get().getPreco());
                    item.setNome(itemRepository.findById(item.getId()).get().getNome());
                    item.setDescricao(itemRepository.findById(item.getId()).get().getDescricao());
                    item.setRestaurante(itemRepository.findById(item.getId()).get().getRestaurante());
                    total += item.getPreco()*item.getQuantidade();
                }
            }
        }
        request.getSession().setAttribute(SESSION_CARRINHO, carrinhoAtualizado);
        model.addAttribute("carrinho", carrinhoAtualizado != null ? carrinhoAtualizado : new ArrayList<>());
        model.addAttribute("total", total);
        return "carrinho";
    }
    
    @GetMapping("/remover-do-carrinho/{id}")
    public String removerItemDoCarrinho(@PathVariable("id") int id, HttpServletRequest request) {
        List<ItemCarrinho> carrinho = (List<ItemCarrinho>) request.getSession().getAttribute(SESSION_CARRINHO);
        if (carrinho != null) {
            carrinho.removeIf(i -> i.getId() == id);
            request.getSession().setAttribute(SESSION_CARRINHO, carrinho);
        }
        return "redirect:/carrinho";
    }

    @GetMapping("/aumentar-quantidade/{id}")
    public String aumentarQuantidade(@PathVariable("id") int id, HttpServletRequest request) {
        List<ItemCarrinho> carrinho = (List<ItemCarrinho>) request.getSession().getAttribute(SESSION_CARRINHO);
        if (carrinho != null) {
            for (ItemCarrinho i : carrinho) {
                if (i.getId() == id) {
                    i.setQuantidade(i.getQuantidade() + 1);
                    break;
                }
            }
            request.getSession().setAttribute(SESSION_CARRINHO, carrinho);
        }
        return "redirect:/carrinho";
    }

    @GetMapping("/diminuir-quantidade/{id}")
    public String diminuirQuantidade(@PathVariable("id") int id, HttpServletRequest request) {
        List<ItemCarrinho> carrinho = (List<ItemCarrinho>) request.getSession().getAttribute(SESSION_CARRINHO);
        if (carrinho != null) {
            boolean remove = false;
            for (ItemCarrinho i : carrinho) {
                if (i.getId() == id) {
                    if (i.getQuantidade() > 1) {
                        i.setQuantidade(i.getQuantidade() - 1);
                    }else {
                        remove = true;
                    }
                }
            }
            if (remove) {
                carrinho.removeIf(i -> i.getId() == id);
            }
            request.getSession().setAttribute(SESSION_CARRINHO, carrinho);
        }
        return "redirect:/carrinho";
    }

    @GetMapping("/limpar-carrinho")
    public String limparCarrinho(HttpServletRequest request) {
        request.getSession().removeAttribute(SESSION_CARRINHO);
        return "redirect:/carrinho";
    }
}