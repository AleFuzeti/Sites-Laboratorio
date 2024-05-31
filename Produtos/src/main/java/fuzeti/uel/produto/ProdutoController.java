package fuzeti.uel.produto;

import jakarta.servlet.http.HttpServletRequest;
import jakarta.validation.Valid;

import java.util.ArrayList;
import java.util.List;
import org.springframework.util.CollectionUtils;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.validation.BindingResult;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;

@Controller
public class ProdutoController {
    private static final String SESSION_FAVORITOS = "sessionFavoritos";
    @Autowired
    ProdutoRepository produtoRepository;

    @GetMapping("/novo-produto")
    public String mostrarFormNovoProduto(Produto produto) {
        return "novo-produto";
    }

    @GetMapping(value = { "/index", "/" })
    public String mostrarListaProdutos(Model model) {
        model.addAttribute("produtos", produtoRepository.findAll());
        return "index";
    }

    @GetMapping("/editar/{id}")
    public String mostrarFormAtualizar(@PathVariable("id") int id, Model model) {
        Produto produto = produtoRepository.findById(id)
                .orElseThrow(() -> new IllegalArgumentException("O id do produto é inválido:" + id));
        model.addAttribute("produto", produto);
        return "atualizar-produto";
    }

    @GetMapping("/remover/{id}")
    public String removerProduto(@PathVariable("id") int id, HttpServletRequest request) {
        Produto produto = produtoRepository.findById(id)
                .orElseThrow(() -> new IllegalArgumentException("O id do produto é inválido:" + id));
        produtoRepository.delete(produto);
        List<Produto> sessionFavoritos = (List<Produto>) request.getSession().getAttribute(SESSION_FAVORITOS);
        sessionFavoritos.remove(produto);
        request.getSession().setAttribute(SESSION_FAVORITOS, sessionFavoritos);
        return "redirect:/index";
    }

    @GetMapping("/favoritar/{id}")
    public String favoritarProduto(@PathVariable("id") int id,
            HttpServletRequest request) {
        Produto produto = produtoRepository.findById(id)
                .orElseThrow(() -> new IllegalArgumentException("O id do produto é inválido: " + id));
        List<Produto> favoritos = (List<Produto>) request.getSession().getAttribute(SESSION_FAVORITOS);
        if (CollectionUtils.isEmpty(favoritos)) {
            favoritos = new ArrayList<>();
        }
        if(!favoritos.contains(produto)){
            favoritos.add(produto); 
        }
        request.getSession().setAttribute(SESSION_FAVORITOS, favoritos);
        return "redirect:/favoritos";
    }

    @GetMapping("/favoritos")
    public String mostrarFavoritos(Model model, HttpServletRequest request) {
        List<Produto> favoritos = (List<Produto>) request.getSession().getAttribute(SESSION_FAVORITOS);
        model.addAttribute("sessionFavoritos",
                !CollectionUtils.isEmpty(favoritos) ? favoritos : new ArrayList<>());

        return "favoritos";
    }

    @GetMapping("/favoritos/remover/{id}")
    public String removerFavorito(@PathVariable("id") int id, HttpServletRequest request) {
        List<Produto> sessionFavoritos = (List<Produto>) request.getSession().getAttribute(SESSION_FAVORITOS);
        Produto produto = produtoRepository.findById(id)
                .orElseThrow(() -> new IllegalArgumentException("O id do produto é inválido: " + id));
        sessionFavoritos.remove(produto);
        request.getSession().setAttribute(SESSION_FAVORITOS, sessionFavoritos);
        return "redirect:/favoritos";
    }

    @PostMapping("/adicionar-produto")
    public String adicionarProduto(@Valid Produto produto, BindingResult result) {
        if (result.hasErrors()) {
            return "/novo-produto";
        }
        produtoRepository.save(produto);
        return "redirect:/index";
    }

    @PostMapping("/atualizar/{id}")
    public String atualizarProduto(@PathVariable("id") int id, @Valid Produto produto,
            BindingResult result, Model model) {
        if (result.hasErrors()) {
            produto.setId(id);
            return "atualizar-produto";
        }
        produtoRepository.save(produto);
        return "redirect:/index";
    }

}
