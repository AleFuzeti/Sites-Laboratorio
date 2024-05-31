package br.uel.projeto;

import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestParam;

@Controller
public class CalcController {

    @GetMapping("/")
    public String index() {
        return "index";
    }

    @PostMapping("/calcular")
    public String calcular(@RequestParam("value1") double num1,
                           @RequestParam("value2") double num2,
                           @RequestParam("op") String operacao,
                           Model model) {
        double resultado = 0;
        switch (operacao) {
            case "soma":
                resultado = num1 + num2;
                break;
            case "subtracao":
                resultado = num1 - num2;
                break;
            case "multiplicacao":
                resultado = num1 * num2;
                break;
            case "divisao":
                if (num2 == 0) {
                    model.addAttribute("resultado", "Não é possível dividir por zero");
                    return "resultado";
                }
                resultado = num1 / num2;
                break;
        }
        model.addAttribute("resultado", resultado);
        return "resultado";
    }
}
