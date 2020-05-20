package com.ipiecoles.java.java350.model;

import com.ipiecoles.java.java350.exception.EmployeException;

import javax.persistence.Entity;
import javax.persistence.GeneratedValue;
import javax.persistence.GenerationType;
import javax.persistence.Id;
import java.time.DayOfWeek;
import java.time.LocalDate;
import java.util.Objects;
import java.util.StringJoiner;

@Entity
public class Employe {


    @Id
    @GeneratedValue(strategy = GenerationType.AUTO)
    private Long id;

    private String nom;

    private String prenom;

    private String matricule;

    private LocalDate dateEmbauche;

    private Double salaire = com.ipiecoles.java.java350.model.Entreprise.SALAIRE_BASE;

    private Integer performance = com.ipiecoles.java.java350.model.Entreprise.PERFORMANCE_BASE;

    private Double tempsPartiel = 1.0;

    public Employe() {
    }

    public Employe(String nom, String prenom, String matricule, LocalDate dateEmbauche, Double salaire, Integer performance, Double tempsPartiel) {
        this.nom = nom;
        this.prenom = prenom;
        this.matricule = matricule;
        this.dateEmbauche = dateEmbauche;
        this.salaire = salaire;
        this.performance = performance;
        this.tempsPartiel = tempsPartiel;
    }

    public Integer getNombreAnneeAnciennete() {
        if(dateEmbauche != null && dateEmbauche.isBefore(LocalDate.now())){
            return LocalDate.now().getYear() - dateEmbauche.getYear();
        }
        return 0;
    }

    public Integer getNbConges() {
        return com.ipiecoles.java.java350.model.Entreprise.NB_CONGES_BASE + this.getNombreAnneeAnciennete();
    }

    public Integer getNbRtt(){
        return getNbRtt(LocalDate.now());
    }

    /**
     * nbWeekEndDays: nombre de samedi et dimanche dans l'année.
     * nbJourFerieSem: nombre de jour ferié se trouvant entre lundi et vendredi.
     * nbJour: calcul le nombre de jour dans l'année en fonction de l'année (bissextile ou non)
     *
     * @param d Date pour connaitre le nombre de jour dans l'année (nbJours)
     *
     * @return le nombre de Rtt de l'employe pendant l'année.
     */
    public Integer getNbRtt(LocalDate d) {
        int nbJours = d.isLeapYear() ? 366 : 365;
        int nbWeekEndDays = 104;
        switch (LocalDate.of(d.getYear(),1,1).getDayOfWeek()) {
            case THURSDAY:
                if(d.isLeapYear()) nbWeekEndDays =  nbWeekEndDays + 1;
                break;
            case FRIDAY:
                if(d.isLeapYear())
                    nbWeekEndDays =  nbWeekEndDays + 2;
                else nbWeekEndDays =  nbWeekEndDays + 1;
            case SATURDAY:
                nbWeekEndDays = nbWeekEndDays + 1;
                break;
        }
        int nbJourFerieSem = (int) Entreprise.joursFeries(d)
                .stream()
                .filter(localDate
                        -> localDate.getDayOfWeek().getValue() <= DayOfWeek.FRIDAY.getValue()).count();
        return (int) Math.ceil((nbJours - Entreprise.NB_JOURS_MAX_FORFAIT - nbWeekEndDays - Entreprise.NB_CONGES_BASE - nbJourFerieSem) * tempsPartiel);
    }

    /**
     * Calcul de la prime annuelle selon la règle :
     * Pour les managers : Prime annuelle de base bonnifiée par l'indice prime manager
     * Pour les autres employés, la prime de base plus éventuellement la prime de performance calculée si l'employé
     * n'a pas la performance de base, en multipliant la prime de base par un l'indice de performance
     * (égal à la performance à laquelle on ajoute l'indice de prime de base)
     *
     * Pour tous les employés, une prime supplémentaire d'ancienneté est ajoutée en multipliant le nombre d'année
     * d'ancienneté avec la prime d'ancienneté. La prime est calculée au pro rata du temps de travail de l'employé
     *
     * @return la prime annuelle de l'employé en Euros et cents
     */
    public Double getPrimeAnnuelle() {
        //Calcule de la prime d'ancienneté
        Double primeAnciennete = Entreprise.PRIME_ANCIENNETE * this.getNombreAnneeAnciennete();
        Double prime;
        //Prime du manager (matricule commençant par M) : Prime annuelle de base multipliée par l'indice prime manager
        //plus la prime d'anciennté.
        if(matricule != null && matricule.startsWith("M")) {
            prime = Entreprise.primeAnnuelleBase() * Entreprise.INDICE_PRIME_MANAGER + primeAnciennete;
        }
        //Pour les autres employés en performance de base, uniquement la prime annuelle plus la prime d'ancienneté.
        else if (this.performance == null || Entreprise.PERFORMANCE_BASE.equals(this.performance)){
            prime = Entreprise.primeAnnuelleBase() + primeAnciennete;
        }
        //Pour les employés plus performance, on bonnifie la prime de base en multipliant par la performance de l'employé
        // et l'indice de prime de base.
        else {
            prime = Entreprise.primeAnnuelleBase() * (this.performance + Entreprise.INDICE_PRIME_BASE) + primeAnciennete;
        }
        //Au pro rata du temps partiel.
        return prime * this.tempsPartiel;
    }

    //Augmenter salaire

    /**
     * salaire : récupère le salaire de l'employé.
     * salaireAgmente: Récupère le salaire après le calcul de l'augmentation.
     * @param pourcentage pourcentage de l'augmentation
     * @throws EmployeException si l'augmentation n'est pas calculable
     */
    public void augmenterSalaire(double pourcentage) throws EmployeException {
        Double salaire =  this.getSalaire();
        if (pourcentage > 0d) {
            salaire = salaire * (1 + pourcentage/100);
            Double salaireAugmente = Double.valueOf(Math.round(salaire * 100));
            this.setSalaire(salaireAugmente / 100);
        } else throw new EmployeException("Diminution de salaire impossible");
    }

    public Long getId() {
        return id;
    }

    public void setId(Long id) {
        this.id = id;
    }

    /**
     * @return the nom
     */
    public String getNom() {
        return nom;
    }

    /**
     * @param nom the nom to set
     */
    public void setNom(String nom) {
        this.nom = nom;
    }

    /**
     * @return the prenom
     */
    public String getPrenom() {
        return prenom;
    }

    /**
     * @param prenom the prenom to set
     */
    public void setPrenom(String prenom) {
        this.prenom = prenom;
    }

    /**
     * @return the matricule
     */
    public String getMatricule() {
        return matricule;
    }

    /**
     * @param matricule the matricule to set
     */
    public void setMatricule(String matricule) {
        this.matricule = matricule;
    }

    /**
     * @return the dateEmbauche
     */
    public LocalDate getDateEmbauche() {
        return dateEmbauche;
    }

    /**
     * @param dateEmbauche the dateEmbauche to set
     */
    public void setDateEmbauche(LocalDate dateEmbauche) {
        this.dateEmbauche = dateEmbauche;
    }

    /**
     * @return the salaire
     */
    public Double getSalaire() {
        return salaire;
    }

    /**
     * @param salaire the salaire to set
     */
    public void setSalaire(Double salaire) {
        this.salaire = salaire;
    }

    public Integer getPerformance() {
        return performance;
    }

    public void setPerformance(Integer performance) {
        this.performance = performance;
    }

    public Double getTempsPartiel() {
        return tempsPartiel;
    }

    public void setTempsPartiel(Double tempsPartiel) {
        this.tempsPartiel = tempsPartiel;
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (!(o instanceof Employe)) return false;
        Employe employe = (Employe) o;
        return Objects.equals(id, employe.id) &&
                Objects.equals(nom, employe.nom) &&
                Objects.equals(prenom, employe.prenom) &&
                Objects.equals(matricule, employe.matricule) &&
                Objects.equals(dateEmbauche, employe.dateEmbauche) &&
                Objects.equals(salaire, employe.salaire) &&
                Objects.equals(performance, employe.performance);
    }

    @Override
    public String toString() {
        return new StringJoiner(", ", Employe.class.getSimpleName() + "[", "]")
                .add("id=" + id)
                .add("nom='" + nom + "'")
                .add("prenom='" + prenom + "'")
                .add("matricule='" + matricule + "'")
                .add("dateEmbauche=" + dateEmbauche)
                .add("salaire=" + salaire)
                .add("performance=" + performance)
                .add("tempsPartiel=" + tempsPartiel)
                .toString();
    }

    @Override
    public int hashCode() {
        return Objects.hash(id, nom, prenom, matricule, dateEmbauche, salaire, performance);
    }
}
