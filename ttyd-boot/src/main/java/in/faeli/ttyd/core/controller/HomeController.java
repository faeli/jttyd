package in.faeli.ttyd.core.controller;

import org.springframework.stereotype.Component;
import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;


@Component
@Controller
@RequestMapping(value = "/")
public class HomeController{
    @GetMapping
    public String home() {
        return "home";
    }
}
