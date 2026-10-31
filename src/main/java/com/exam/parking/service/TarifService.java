package com.exam.parking.service;

import com.exam.parking.entity.TypePlace;
import com.exam.parking.entity.CategorieClient;
import com.exam.parking.entity.Tarif;
import com.exam.parking.repository.TypePlaceRepository;
import com.exam.parking.repository.CategorieClientRepository;
import com.exam.parking.repository.TarifRepository;

import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.math.BigDecimal;
import java.math.RoundingMode;
import java.time.Duration;
import java.time.LocalDateTime;
import java.time.LocalTime;
import java.util.List;
import java.util.NoSuchElementException;

@Service
@Transactional(readOnly = true)
public class TarifService {

    private final TypePlaceRepository typePlaceRepository;
    private final CategorieClientRepository categorieRepository;
    private final TarifRepository tarifRepository;


	public TarifService(
	        TypePlaceRepository typePlaceRepository,
	        CategorieClientRepository categorieRepository,
	        TarifRepository tarifRepository) {

	    this.typePlaceRepository = typePlaceRepository;
	    this.categorieRepository = categorieRepository;
	    this.tarifRepository = tarifRepository;
	}





    // Calculer le nombre d'heures facturables
    public long calculerHeures(
            LocalDateTime entree,
            LocalDateTime sortie) {

        if (entree == null || sortie == null
                || !sortie.isAfter(entree)) {

            throw new IllegalArgumentException(
                "Les dates de stationnement sont invalides."
            );
        }

        long minutes = Duration.between(
                entree, sortie
        ).toMinutes();

        // Chaque heure commencée est facturée.
        return Math.max(1, (minutes + 59) / 60);
    }

    // Calculer le montant brut
    public BigDecimal calculerMontantBrut(
            Long typePlaceId,
            LocalDateTime entree,
            LocalDateTime sortie) {

        TypePlace typePlace = typePlaceRepository
                .findById(typePlaceId)
                .orElseThrow(() ->
                    new NoSuchElementException(
                        "Type de place introuvable."
                    )
                );

        long heures = calculerHeures(entree, sortie);

        BigDecimal tarifHoraire =
                typePlace.getTarifHoraireBase();

        if (tarifHoraire == null
                || tarifHoraire.signum() < 0) {
            throw new IllegalStateException(
                "Le tarif horaire est invalide."
            );
        }

        return tarifHoraire.multiply(
                BigDecimal.valueOf(heures)
        );
    }

    // Appliquer la remise d'une catégorie client
    public BigDecimal appliquerRemise(
            BigDecimal montant,
            Long categorieId) {

        if (montant == null || montant.signum() < 0) {
            throw new IllegalArgumentException(
                "Le montant est invalide."
            );
        }

        if (categorieId == null) {
            return montant.setScale(0, RoundingMode.HALF_UP);
        }

        CategorieClient categorie = categorieRepository
                .findById(categorieId)
                .orElseThrow(() ->
                    new NoSuchElementException(
                        "Catégorie client introuvable."
                    )
                );

        BigDecimal remise = categorie.getCoefficientRemise();

        if (remise == null
                || remise.compareTo(BigDecimal.ZERO) < 0
                || remise.compareTo(BigDecimal.ONE) > 0) {

            throw new IllegalStateException(
                "Le coefficient de remise est invalide."
            );
        }

        return montant
                .multiply(BigDecimal.ONE.subtract(remise))
                .setScale(0, RoundingMode.HALF_UP);
    }

    // Calculer le montant final
    public BigDecimal calculerMontantFinal(
            Long typePlaceId,
            Long categorieId,
            LocalDateTime entree,
            LocalDateTime sortie) {

        BigDecimal montantBrut = calculerMontantBrut(
                typePlaceId,
                entree,
                sortie
        );

        return appliquerRemise(montantBrut, categorieId);
    }


	public Tarif rechercherTarifApplicable(
	        Long typePlaceId,
	        Long categorieId,
	        LocalDateTime date) {

	    if (typePlaceId == null || date == null) {
	        throw new IllegalArgumentException(
	            "Le type de place et la date sont obligatoires."
	        );
	    }

	    List<Tarif> tarifs = tarifRepository
	        .findByTypePlace_IdAndActifTrue(typePlaceId);

	    List<Tarif> tarifsCategorie = tarifs.stream()
	        .filter(t -> t.getCategorieClient() != null)
	        .filter(t -> categorieId != null
	            && categorieId.equals(t.getCategorieClient().getId()))
	        .filter(t -> t.estApplicable(date))
	        .toList();

	    if (tarifsCategorie.size() > 1) {
	        throw new IllegalStateException(
	            "Plusieurs tarifs spécifiques sont applicables."
	        );
	    }

	    if (!tarifsCategorie.isEmpty()) {
	        return tarifsCategorie.get(0);
	    }

	    List<Tarif> tarifsGeneraux = tarifs.stream()
	        .filter(t -> t.getCategorieClient() == null)
	        .filter(t -> t.estApplicable(date))
	        .toList();

	    if (tarifsGeneraux.size() != 1) {
	        throw new IllegalStateException(
	            "Aucun tarif général unique n'est applicable."
	        );
	    }

	    return tarifsGeneraux.get(0);
	}

	public BigDecimal calculerMontantStationnement(
	        Tarif tarif,
	        Long categorieId,
	        LocalDateTime entree,
	        LocalDateTime sortie) {

	    if (tarif == null) {
	        throw new IllegalArgumentException(
	            "Aucun tarif associé au stationnement."
	        );
	    }

	    long heures = calculerHeures(entree, sortie);

	    BigDecimal prixHoraire = tarif.getPrixHoraire();

	    if (prixHoraire == null || prixHoraire.signum() < 0) {
	        throw new IllegalStateException(
	            "Le prix horaire est invalide."
	        );
	    }

	    BigDecimal montantBrut = prixHoraire.multiply(
	        BigDecimal.valueOf(heures)
	    );

	    return appliquerRemise(montantBrut, categorieId);
	}


	public List<Tarif> lister() {
	    return tarifRepository.findAll();
	}

	public Tarif rechercherParId(Long id) {
	    return tarifRepository.findById(id)
	        .orElseThrow(() ->
	            new NoSuchElementException("Tarif introuvable : " + id)
	        );
	}

	@Transactional
	public Tarif ajouter(
	        Long typePlaceId,
	        Long categorieId,
	        BigDecimal prixHoraire,
	        BigDecimal prixJournalier,
	        LocalTime heureDebut,
	        LocalTime heureFin) {

	    validerPrixEtHoraires(
	        prixHoraire, prixJournalier, heureDebut, heureFin
	    );

	    verifierChevauchement(
	    	    null, typePlaceId, categorieId, heureDebut, heureFin
	    );

	    TypePlace typePlace = typePlaceRepository
	        .findById(typePlaceId)
	        .orElseThrow(() ->
	            new NoSuchElementException("Type de place introuvable.")
	        );

	    CategorieClient categorie = null;

	    if (categorieId != null) {
	        categorie = categorieRepository
	            .findById(categorieId)
	            .orElseThrow(() ->
	                new NoSuchElementException("Catégorie introuvable.")
	            );
	    }

	    Tarif tarif = new Tarif(
	        typePlace,
	        categorie,
	        prixHoraire,
	        prixJournalier,
	        heureDebut,
	        heureFin
	    );

	    return tarifRepository.save(tarif);
	}

	@Transactional
	public Tarif modifier(
	        Long id,
	        Long typePlaceId,
	        Long categorieId,
	        BigDecimal prixHoraire,
	        BigDecimal prixJournalier,
	        LocalTime heureDebut,
	        LocalTime heureFin) {

	    validerPrixEtHoraires(
	        prixHoraire, prixJournalier, heureDebut, heureFin
	    );

	    verifierChevauchement(
	    	    id, typePlaceId, categorieId, heureDebut, heureFin
	    );

	    Tarif tarif = rechercherParId(id);

	    TypePlace typePlace = typePlaceRepository
	        .findById(typePlaceId)
	        .orElseThrow(() ->
	            new NoSuchElementException("Type de place introuvable.")
	        );

	    CategorieClient categorie = null;

	    if (categorieId != null) {
	        categorie = categorieRepository
	            .findById(categorieId)
	            .orElseThrow(() ->
	                new NoSuchElementException("Catégorie introuvable.")
	            );
	    }

	    tarif.setTypePlace(typePlace);
	    tarif.setCategorieClient(categorie);
	    tarif.setPrixHoraire(prixHoraire);
	    tarif.setPrixJournalier(prixJournalier);
	    tarif.setHeureDebut(heureDebut);
	    tarif.setHeureFin(heureFin);

	    return tarifRepository.save(tarif);
	}

	@Transactional
	public Tarif changerStatut(Long id) {
	    Tarif tarif = rechercherParId(id);
	    tarif.setActif(!tarif.isActif());
	    return tarifRepository.save(tarif);
	}

	private void validerPrixEtHoraires(
	        BigDecimal prixHoraire,
	        BigDecimal prixJournalier,
	        LocalTime heureDebut,
	        LocalTime heureFin) {

	    if (prixHoraire == null || prixHoraire.signum() < 0) {
	        throw new IllegalArgumentException(
	            "Le prix horaire doit être positif ou nul."
	        );
	    }

	    if (prixJournalier != null && prixJournalier.signum() < 0) {
	        throw new IllegalArgumentException(
	            "Le prix journalier ne peut pas être négatif."
	        );
	    }

	    if ((heureDebut == null) != (heureFin == null)) {
	        throw new IllegalArgumentException(
	            "Les deux horaires doivent être renseignés ou laissés vides."
	        );
	    }
	}


	private boolean horairesChevauchent(
	        LocalTime debut1,
	        LocalTime fin1,
	        LocalTime debut2,
	        LocalTime fin2) {

	    // Un tarif sans plage horaire s'applique toute la journée.
	    if (debut1 == null || fin1 == null
	            || debut2 == null || fin2 == null) {
	        return true;
	    }

	    // Des horaires identiques signifient 24 heures.
	    if (debut1.equals(fin1) || debut2.equals(fin2)) {
	        return true;
	    }

	    return intervallesChevauchent(debut1, fin1, debut2, fin2);
	}

	private boolean intervallesChevauchent(
	        LocalTime debut1,
	        LocalTime fin1,
	        LocalTime debut2,
	        LocalTime fin2) {

	    long d1 = debut1.toNanoOfDay();
	    long f1 = fin1.toNanoOfDay();
	    long d2 = debut2.toNanoOfDay();
	    long f2 = fin2.toNanoOfDay();

	    long jour = 24L * 60 * 60 * 1_000_000_000L;

	    if (f1 <= d1) f1 += jour;
	    if (f2 <= d2) f2 += jour;

	    return (d1 < f2 && d2 < f1)
	        || (d1 < f2 + jour && d2 + jour < f1)
	        || (d1 + jour < f2 && d2 < f1 + jour);
	}


	private void verifierChevauchement(
	        Long tarifId,
	        Long typePlaceId,
	        Long categorieId,
	        LocalTime heureDebut,
	        LocalTime heureFin) {

	    List<Tarif> tarifs = tarifRepository
	        .findByTypePlace_IdAndActifTrue(typePlaceId);

	    for (Tarif existant : tarifs) {

	        if (tarifId != null && tarifId.equals(existant.getId())) {
	            continue;
	        }

	        Long categorieExistante =
	            existant.getCategorieClient() == null
	                ? null
	                : existant.getCategorieClient().getId();

	        if (!java.util.Objects.equals(
	                categorieId, categorieExistante)) {
	            continue;
	        }

	        if (horairesChevauchent(
	                heureDebut,
	                heureFin,
	                existant.getHeureDebut(),
	                existant.getHeureFin())) {

	            throw new IllegalStateException(
	                "Un tarif actif existe déjà sur cette plage horaire."
	            );
	        }
	    }
	}



	public BigDecimal calculerMontantAvecPlafondJournalier(
	        BigDecimal prixHoraire,
	        BigDecimal prixJournalier,
	        LocalDateTime entree,
	        LocalDateTime sortie) {

	    if (prixHoraire == null || prixHoraire.signum() < 0) {
	        throw new IllegalArgumentException(
	            "Prix horaire invalide."
	        );
	    }

	    long heures = calculerHeures(entree, sortie);

	    if (prixJournalier == null) {
	        return prixHoraire.multiply(
	            BigDecimal.valueOf(heures)
	        );
	    }

	    if (prixJournalier.signum() < 0) {
	        throw new IllegalArgumentException(
	            "Prix journalier invalide."
	        );
	    }

	    long joursComplets = heures / 24;
	    long heuresRestantes = heures % 24;

	    BigDecimal montantJours = prixJournalier.multiply(
	        BigDecimal.valueOf(joursComplets)
	    );

	    BigDecimal montantReste = prixHoraire.multiply(
	        BigDecimal.valueOf(heuresRestantes)
	    ).min(prixJournalier);

	    return montantJours.add(montantReste);
	}




}