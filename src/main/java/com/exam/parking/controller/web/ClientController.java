
package com.exam.parking.controller.web;

import com.exam.parking.entity.Utilisateur;
import com.exam.parking.service.ClientDashboardService;
import com.exam.parking.service.UtilisateurService;

import org.springframework.security.core.Authentication;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;

@Controller
@RequestMapping("/client")
public class ClientController {

    private final UtilisateurService utilisateurService;
    private final ClientDashboardService dashboardService;

    public ClientController(
            UtilisateurService utilisateurService,
            ClientDashboardService dashboardService) {

        this.utilisateurService = utilisateurService;
        this.dashboardService = dashboardService;
    }

    @GetMapping("/dashboard")
    public String dashboard(
            Authentication authentication,
            Model model) {

        Utilisateur client =
                utilisateurService.rechercherParEmail(
                        authentication.getName()
                );

        model.addAttribute("pageTitle", "Espace client");
        model.addAttribute("activePage", "client");
        model.addAttribute("client", client);

        model.addAttribute(
                "nombreVehicules",
                dashboardService.compterVehicules(client)
        );

        model.addAttribute(
                "nombreReservations",
                dashboardService.compterReservations(client)
        );

        model.addAttribute(
                "nombreAbonnements",
                dashboardService.compterAbonnements(client)
        );

        model.addAttribute(
                "nombrePaiements",
                dashboardService.compterPaiements(client)
        );

        return "client/dashboard";
    }
}
