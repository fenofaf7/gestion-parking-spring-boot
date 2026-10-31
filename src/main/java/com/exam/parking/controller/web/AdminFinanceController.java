package com.exam.parking.controller.web;

import com.exam.parking.service.PaiementService;

import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;

@Controller
@RequestMapping("/admin/finances")
public class AdminFinanceController {

    private final PaiementService paiementService;

    public AdminFinanceController(
            PaiementService paiementService) {
        this.paiementService = paiementService;
    }

    @GetMapping
    public String index(Model model) {

        model.addAttribute(
                "recettes",
                paiementService.calculerRecettesEncaissees()
        );

        model.addAttribute(
                "montantEnAttente",
                paiementService.calculerMontantEnAttente()
        );

        model.addAttribute(
                "paiementsConfirmes",
                paiementService.compterPaiementsConfirmes()
        );

        model.addAttribute(
                "paiementsEnAttente",
                paiementService.compterPaiementsEnAttente()
        );

        model.addAttribute(
                "paiementsEchoues",
                paiementService.compterPaiementsEchoues()
        );

        model.addAttribute(
                "paiements",
                paiementService.lister()
        );

        return "admin/finances";
    }
}