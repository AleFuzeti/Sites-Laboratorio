package fuzeti.uel.produto;

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
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.servlet.mvc.support.RedirectAttributes;

@Controller
public class ProdutoController {
    private static final String SESSION_CARRINHO = "sessionCarrinho";
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
        List<Produto> sessionCarrinho = (List<Produto>) request.getSession().getAttribute(SESSION_CARRINHO);
        sessionCarrinho.remove(produto);
        request.getSession().setAttribute(SESSION_CARRINHO, sessionCarrinho);
        return "redirect:/index";
    }

    @PostMapping("/adicionar-ao-carrinho")
    public String adicionarProdutoAoCarrinho(@RequestParam("id") int id, @RequestParam("quantidade") int quantidade, HttpServletRequest request, RedirectAttributes redirectAttributes) {
        Produto produto = produtoRepository.findById(id)
                .orElseThrow(() -> new IllegalArgumentException("O id do produto é inválido: " + id));
        
        if (quantidade > produto.getQuantidade()) {
            redirectAttributes.addFlashAttribute("error", "Quantidade inválida. Por favor, insira uma quantidade entre 1 e " + produto.getQuantidade() + ".");
            return "redirect:/index";
        }
        if (quantidade < 1) {
            redirectAttributes.addFlashAttribute("error", "Quantidade inválida. Por favor, insira uma quantidade maior que 0.");
            return "redirect:/index";
        }
    
        // Verifica se a quantidade a ser adicionada excede o estoque disponível
        int quantidadeDisponivel = produto.getQuantidade() - quantidadeJaNoCarrinho(request, produto.getId());
        if (quantidade > quantidadeDisponivel) {
            redirectAttributes.addFlashAttribute("error", "A quantidade selecionada excede o estoque disponível (" + quantidadeDisponivel + ").");
            return "redirect:/index";
        }
    
        List<Produto> carrinho = (List<Produto>) request.getSession().getAttribute(SESSION_CARRINHO);
        if (carrinho == null) {
            carrinho = new ArrayList<>();
        }
    
        // Verifica se o produto já está no carrinho
        boolean produtoJaNoCarrinho = false;
        for (Produto p : carrinho) {
            if (p.getId() == produto.getId()) {
                // Se o produto já está no carrinho, aumenta a quantidade
                p.setQuantidade(p.getQuantidade() + quantidade);
                produtoJaNoCarrinho = true;
                break;
            }
        }
    
        // Se o produto não estava no carrinho, adiciona-o
        if (!produtoJaNoCarrinho) {
            produto.setQuantidade(quantidade); // Define a quantidade selecionada para o produto
            carrinho.add(produto); // Adiciona o produto ao carrinho
        }
    
        request.getSession().setAttribute(SESSION_CARRINHO, carrinho);
        return "redirect:/carrinho";
    }
    
    // método para remover APENAS UM produto do carrinho
    // fiz pra remover com botão direto no carrinho
    @PostMapping("/remover-um-do-carrinho")
    public String removerUmDoCarrinho(@RequestParam("id") int id, HttpServletRequest request) {
        List<Produto> carrinho = (List<Produto>) request.getSession().getAttribute(SESSION_CARRINHO);
        if (carrinho != null) {
            for (Produto produto : carrinho) {
                if (produto.getId() == id) {
                    if (produto.getQuantidade() > 1) {
                        produto.setQuantidade(produto.getQuantidade() - 1);
                    } else {
                        carrinho.remove(produto);
                    }
                    break;
                }
            }
            request.getSession().setAttribute(SESSION_CARRINHO, carrinho);
        }
        return "redirect:/carrinho";
    }

    // Método para verificar a quantidade do produto já adicionada ao carrinho
    private int quantidadeJaNoCarrinho(HttpServletRequest request, int idProduto) {
        List<Produto> carrinho = (List<Produto>) request.getSession().getAttribute(SESSION_CARRINHO);
        int quantidadeTotal = 0;
        if (carrinho != null) {
            for (Produto produto : carrinho) {
                if (produto.getId() == idProduto) {
                    quantidadeTotal += produto.getQuantidade();
                }
            }
        }
        return quantidadeTotal;
    }
    
    @GetMapping("/carrinho")
    public String mostrarCarrinho(Model model, HttpServletRequest request) {
        List<Produto> carrinho = (List<Produto>) request.getSession().getAttribute(SESSION_CARRINHO);
        model.addAttribute("carrinho", carrinho != null ? carrinho : new ArrayList<>());
        return "carrinho";
    }
    
    @GetMapping("/remover-do-carrinho/{id}")
    public String removerProdutoDoCarrinho(@PathVariable("id") int id, HttpServletRequest request) {
        List<Produto> carrinho = (List<Produto>) request.getSession().getAttribute(SESSION_CARRINHO);
        if (carrinho != null) {
            carrinho.removeIf(p -> p.getId() == id);
            request.getSession().setAttribute(SESSION_CARRINHO, carrinho);
        }
        return "redirect:/carrinho";
    }

    // Método para limpar o carrinho
    @GetMapping("/limpar-carrinho")
    public String limparCarrinho(HttpServletRequest request) {
        request.getSession().removeAttribute(SESSION_CARRINHO);
        return "redirect:/carrinho";
    }

    // Método para atualizar a quantidade de itens
    @PostMapping("/atualizar-quantidade-no-carrinho")
    public String atualizarQuantidadeNoCarrinho(@RequestParam("id") int id, @RequestParam("action") String action, HttpServletRequest request, RedirectAttributes redirectAttributes) {
        // Se botão for "add", chama o método para adicionar ao carrinho e passa 1 como param
        if ("add".equals(action)) {
            return adicionarProdutoAoCarrinho(id, 1, request, redirectAttributes);
        }
        // Se botão for "remove" chama o método para remover 1 do carrinho
        else if ("remove".equals(action)) {
            return removerUmDoCarrinho(id, request);
        } 
        // Caso contrário, volta pro carrinho
        else {
            return "redirect:/carrinho";
        }
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
