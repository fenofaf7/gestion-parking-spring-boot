
package com.exam.parking.service;

import com.exam.parking.entity.Utilisateur;
import com.exam.parking.enumeration.StatutPaiement;
import com.exam.parking.repository.AbonnementRepository;
import com.exam.parking.repository.PaiementRepository;
import com.exam.parking.repository.ReservationRepository;
import com.exam.parking.repository.VehiculeRepository;

import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
@Transactional(readOnly = true)
public class ClientDashboardService {

    private final VehiculeRepository vehiculeRepository;
    private final ReservationRepository reservationRepository;
    private final AbonnementRepository abonnementRepository;
    private final PaiementRepository paiementRepository;

    public ClientDashboardService(
            VehiculeRepository vehiculeRepository,
            ReservationRepository reservationRepository,
            AbonnementRepository abonnementRepository,
            PaiementRepository paiementRepository) {

        this.vehiculeRepository = vehiculeRepository;
        this.reservationRepository = reservationRepository;
        this.abonnementRepository = abonnementRepository;
        this.paiementRepository = paiementRepository;
    }

    public long compterVehicules(Utilisateur client) {
        return vehiculeRepository.countByProprietaire_Id(
                client.getId()
        );
    }

    public long compterReservations(Utilisateur client) {
        return reservationRepository.countByUtilisateur_Id(
                client.getId()
        );
    }

    public long compterAbonnements(Utilisateur client) {

        return abonnementRepository.countByUtilisateur_Id(
                client.getId()
        );
    }

    public long compterPaiements(Utilisateur client) {

        return paiementRepository
                .countByStationnement_Vehicule_Proprietaire_IdAndStatut(
                        client.getId(),
                        StatutPaiement.CONFIRME
                );
    }
}
