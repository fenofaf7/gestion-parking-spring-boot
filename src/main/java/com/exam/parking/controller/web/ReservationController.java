
package com.exam.parking.controller.web;

import com.exam.parking.service.ReservationService;
import com.exam.parking.service.UtilisateurService;
import com.exam.parking.service.VehiculeService;
import com.exam.parking.service.PlaceService;

import org.springframework.format.annotation.DateTimeFormat;
import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.servlet.mvc.support.RedirectAttributes;

import java.time.LocalDateTime;
import java.util.List;
import java.util.NoSuchElementException;

@Controller
@RequestMapping("/admin/reservations")
public class ReservationController {

    private final ReservationService reservationService;
    private final UtilisateurService utilisateurService;
    private final VehiculeService vehiculeService;
    private final PlaceService placeService;

    public ReservationController(
            ReservationService reservationService,
            UtilisateurService utilisateurService,
            VehiculeService vehiculeService,
            PlaceService placeService) {

        this.reservationService = reservationService;
        this.utilisateurService = utilisateurService;
        this.vehiculeService = vehiculeService;
        this.placeService = placeService;
    }

    @GetMapping
    public String index(Model model) {

        model.addAttribute("pageTitle", "Gestion des réservations");
        model.addAttribute("activePage", "reservations");
        model.addAttribute("reservations", reservationService.lister());

        return "reservations/index";
    }

    @GetMapping("/ajouter")
    public String formulaireAjout(Model model) {

        model.addAttribute("pageTitle", "Nouvelle réservation");
        model.addAttribute("activePage", "reservations");

        model.addAttribute("utilisateurs", utilisateurService.lister());
        model.addAttribute("vehicules", vehiculeService.lister());
        model.addAttribute("places", placeService.lister());

        return "reservations/form";
    }

    @PostMapping("/ajouter")
    public String ajouter(
            @RequestParam Long utilisateurId,
            @RequestParam Long vehiculeId,
            @RequestParam Long placeId,

            @RequestParam
            @DateTimeFormat(iso = DateTimeFormat.ISO.DATE_TIME)
            LocalDateTime dateDebut,

            @RequestParam
            @DateTimeFormat(iso = DateTimeFormat.ISO.DATE_TIME)
            LocalDateTime dateFin,

            RedirectAttributes redirectAttributes) {

        try {
            reservationService.reserver(
                utilisateurId,
                vehiculeId,
                placeId,
                dateDebut,
                dateFin
            );

            redirectAttributes.addFlashAttribute(
                "success", "Réservation créée avec succès."
            );

            return "redirect:/admin/reservations";

        } catch (IllegalArgumentException |
                 IllegalStateException |
                 NoSuchElementException e) {

            redirectAttributes.addFlashAttribute(
                "error", e.getMessage()
            );

            return "redirect:/admin/reservations/ajouter";
        }
    }

    @PostMapping("/{id}/confirmer")
    public String confirmer(
            @PathVariable Long id,
            RedirectAttributes redirectAttributes) {

        try {
            reservationService.confirmer(id);

            redirectAttributes.addFlashAttribute(
                "success", "Réservation confirmée."
            );

        } catch (IllegalArgumentException |
                 IllegalStateException |
                 NoSuchElementException e) {

            redirectAttributes.addFlashAttribute(
                "error", e.getMessage()
            );
        }

        return "redirect:/admin/reservations";
    }

    @PostMapping("/{id}/annuler")
    public String annuler(
            @PathVariable Long id,
            RedirectAttributes redirectAttributes) {

        try {
            reservationService.annuler(id);

            redirectAttributes.addFlashAttribute(
                "success", "Réservation annulée."
            );

        } catch (IllegalArgumentException |
                 IllegalStateException |
                 NoSuchElementException e) {

            redirectAttributes.addFlashAttribute(
                "error", e.getMessage()
            );
        }

        return "redirect:/admin/reservations";
    }


	@GetMapping("/places-disponibles")
	@ResponseBody
	public ResponseEntity<List<PlaceDisponibleDto>> placesDisponibles(
	        @RequestParam
	        @DateTimeFormat(iso = DateTimeFormat.ISO.DATE_TIME)
	        LocalDateTime debut,

	        @RequestParam
	        @DateTimeFormat(iso = DateTimeFormat.ISO.DATE_TIME)
	        LocalDateTime fin) {

	    List<PlaceDisponibleDto> places = reservationService
	        .placesDisponibles(debut, fin)
	        .stream()
	        .map(place -> new PlaceDisponibleDto(
	            place.getId(),
	            place.getNumero(),
	            place.getZone()
	        ))
	        .toList();

	    return ResponseEntity.ok(places);
	}

	public record PlaceDisponibleDto(
	    Long id,
	    String numero,
	    String zone
	) {}

}
