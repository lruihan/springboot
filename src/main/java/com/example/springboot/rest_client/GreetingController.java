package com.example.springboot.rest_client;

import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;

@Controller
public class GreetingController {

    @RequestMapping("/greeting")
    public String greeting(@RequestParam(value="name", required=false, defaultValue="World") String name, Model model) {
        model.addAttribute("name", name);
        return "greeting";
    }

    @RequestMapping("/knowledgebase")
    public String greeting(@RequestParam(value="searchText", required=false, defaultValue="*") String searchText,
                           @RequestParam(value="limit", required=false, defaultValue="3") int limit,
                           Model model) {
        model.addAttribute("searchText", searchText);
        model.addAttribute("limit", limit);
        return "greeting";
    }
}