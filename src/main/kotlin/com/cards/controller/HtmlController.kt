package com.cards.controller

import org.springframework.ui.ModelMap
import org.springframework.web.bind.annotation.*
import org.springframework.web.servlet.ModelAndView


@RestController
class HtmlController() {

    @GetMapping("/")
    fun home(model: ModelMap): ModelAndView {
        //model.addAttribute("attribute", "redirectWithRedirectPrefix")
        return ModelAndView("redirect:/klaverjassen.html", model)
    }
}


