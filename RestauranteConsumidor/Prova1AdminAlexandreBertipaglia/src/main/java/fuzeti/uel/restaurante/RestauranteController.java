package fuzeti.uel.restaurante;

import jakarta.servlet.http.HttpServletRequest;
import jakarta.validation.Valid;

import java.util.ArrayList;
import java.util.List;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.validation.BindingResult;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;

@Controller
public class RestauranteController {
    @Autowired
    RestauranteRepository restauranteRepository;
    @Autowired
    ItemRepository itemRepository;
    
    @GetMapping(value = { "/index", "/" })
    public String mostrarListaRestaurantes(Model model) {
        model.addAttribute("restaurantes", restauranteRepository.findAll());
        return "index";
    }

    // RESTAURANTE

    @GetMapping("/novo-restaurante")
    public String mostrarFormNovoRestaurante(Restaurante restaurante) {
        return "novo-restaurante";
    }

    @PostMapping("/adicionar-restaurante")
    public String adicionarRestaurante(@Valid Restaurante restaurante, BindingResult result) {
        if (result.hasErrors()) {
            return "/novo-restaurante";
        }
        restauranteRepository.save(restaurante);
        return "redirect:/index";
    }

    @GetMapping("/editar/{id}")
    public String mostrarFormAtualizar(@PathVariable("id") int id, Model model) {
        Restaurante restaurante = restauranteRepository.findById(id)
                .orElseThrow(() -> new IllegalArgumentException("O id do restaurante é inválido:" + id));
        model.addAttribute("restaurante", restaurante);
        return "atualizar-restaurante";
    }

    @PostMapping("/atualizar/{id}")
    public String atualizarRestaurante(@PathVariable("id") int id, @Valid Restaurante restaurante,
            BindingResult result, Model model) {
        if (result.hasErrors()) {
            restaurante.setId(id);
            return "atualizar-restaurante";
        }
        restauranteRepository.save(restaurante);
        return "redirect:/index";
    }

    @GetMapping("/remover/{id}")
    public String removerRestaurante(@PathVariable("id") int id, HttpServletRequest request) {
        Restaurante restaurante = restauranteRepository.findById(id)
                .orElseThrow(() -> new IllegalArgumentException("O id do restaurante é inválido:" + id));
        restauranteRepository.delete(restaurante);
        return "redirect:/index";   
    }

    // CARDAPIO

    @GetMapping("/cardapio/{id}")
    public String mostrarCardapio(@PathVariable("id") int id, Model model) {
        Restaurante restaurante = restauranteRepository.findById(id)
                .orElseThrow(() -> new IllegalArgumentException("O id do restaurante é inválido:" + id));
        List<Item> itensCardapio = new ArrayList<>(restaurante.getItens());
        model.addAttribute("restaurante", restaurante);
        model.addAttribute("item_cardapio", itensCardapio);
        return "cardapio";
    }

    @GetMapping("/cardapio/{id}/novo-item")
    public String mostrarFormNovoItem(@PathVariable("id") int id, Item item, Model model) {
        Restaurante restaurante = restauranteRepository.findById(id)
                .orElseThrow(() -> new IllegalArgumentException("O id do restaurante é inválido:" + id));
        model.addAttribute("restaurante", restaurante);
        return "novo-item";
    }
    
    @PostMapping("/cardapio/{id}/adicionar-item")
    public String adicionarItem(@PathVariable("id") int id, @Valid Item item, BindingResult result, Model model) {
        if (result.hasErrors()) {
            Restaurante restaurante = restauranteRepository.findById(id)
                    .orElseThrow(() -> new IllegalArgumentException("O id do restaurante é inválido:" + id));
            model.addAttribute("restaurante", restaurante);
            return "novo-item";
        }
        Restaurante restaurante = restauranteRepository.findById(id)
                .orElseThrow(() -> new IllegalArgumentException("O id do restaurante é inválido:" + id));
        item.setRestaurante(restaurante);
        itemRepository.save(item);
        return "redirect:/cardapio/" + id;
    }

    @GetMapping("/cardapio/editar/{id}/{item_id}")
    public String mostrarFormEditarItem(@PathVariable("id") int id, @PathVariable("item_id") int item_id, Model model) {
        Item item = itemRepository.findById(item_id)
                .orElseThrow(() -> new IllegalArgumentException("O id do item é inválido:" + item_id));
        model.addAttribute("item", item);
        model.addAttribute("restaurante", item.getRestaurante());
        return "atualizar-item";
    }    
    
    @PostMapping("/cardapio/atualizar/{id}/{item_id}")
    public String atualizarItem(@PathVariable("id") int id, @PathVariable("item_id") int item_id, @Valid Item item,
            BindingResult result, Model model) {
        if (result.hasErrors()) {
            item.setId(item_id);
            return "atualizar-item";
        }
        Restaurante restaurante = restauranteRepository.findById(id)
                .orElseThrow(() -> new IllegalArgumentException("O id do restaurante é inválido:" + id));
        Item itemAtualizado = itemRepository.findById(item_id)
                .orElseThrow(() -> new IllegalArgumentException("O id do item é inválido:" + item_id));
        itemAtualizado.setNome(item.getNome());
        itemAtualizado.setDescricao(item.getDescricao());
        itemAtualizado.setPreco(item.getPreco());
        itemAtualizado.setRestaurante(restaurante);
        itemRepository.save(itemAtualizado);
        
        return "redirect:/cardapio/" + restaurante.getId();
    }

    @GetMapping("/cardapio/remover/{id}/{item_id}")
    public String removerItem(@PathVariable("id") int id, @PathVariable("item_id") int item_id, Model model) {
        Item item = itemRepository.findById(item_id)
                .orElseThrow(() -> new IllegalArgumentException("O id do item é inválido:" + item_id));
        itemRepository.delete(item);
        return "redirect:/cardapio/" + id;
    }
}