package com.ipiecoles.java.java350.service;

import com.ipiecoles.java.java350.exception.EmployeException;
import com.ipiecoles.java.java350.model.Employe;
import com.ipiecoles.java.java350.model.Entreprise;
import com.ipiecoles.java.java350.model.NiveauEtude;
import com.ipiecoles.java.java350.model.Poste;
import com.ipiecoles.java.java350.repository.EmployeRepository;
import org.assertj.core.api.Assertions;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.ArgumentCaptor;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.Mockito;
import org.mockito.junit.jupiter.MockitoExtension;

import javax.persistence.EntityExistsException;
import java.time.LocalDate;

import static org.mockito.Mockito.when;


@ExtendWith(MockitoExtension.class)
public class EmployeServiceTest {

    @InjectMocks
    private com.ipiecoles.java.java350.service.EmployeService employeService;

    @Mock
    private EmployeRepository employeRepository;

    @Test
    void testEmbaucheEmploye() throws EmployeException {
        //Given
        String nom = "Doe";
        String prenom = "Johny";
        Poste poste = Poste.TECHNICIEN;
        NiveauEtude niveauEtude = NiveauEtude.BTS_IUT;
        Double tempsPartiel = 1.0;
        when(employeRepository.findLastMatricule()).thenReturn("00345");

        //When
        employeService.embaucheEmploye(nom,prenom,poste,niveauEtude,tempsPartiel);

        //Then
        ArgumentCaptor<Employe> employeArgumentCaptor = ArgumentCaptor.forClass(Employe.class);
        Mockito.verify(employeRepository, Mockito.times(1)).save(employeArgumentCaptor.capture());
        Assertions.assertThat(employeArgumentCaptor.getValue().getMatricule()).isEqualTo("T00346");
        Assertions.assertThat(employeArgumentCaptor.getValue().getNom()).isEqualTo(nom);
        Assertions.assertThat(employeArgumentCaptor.getValue().getPrenom()).isEqualTo(prenom);
        Assertions.assertThat(employeArgumentCaptor.getValue().getDateEmbauche()).isEqualTo(LocalDate.now());
        Assertions.assertThat(employeArgumentCaptor.getValue().getPerformance()).isEqualTo(Entreprise.PERFORMANCE_BASE);
        Assertions.assertThat(employeArgumentCaptor.getValue().getSalaire()).isEqualTo(1825.46);
    }

    @Test
    public void testEmbaucheManagerMasterPleinTempsLimiteMatricule() {
        String nom = "Doe";
        String prenom = "John";
        Poste poste = Poste.MANAGER;
        NiveauEtude niveauEtude = NiveauEtude.MASTER;
        Double tempsPartiel = 0.5;
        Mockito.when(employeRepository.findLastMatricule()).thenReturn(null);
        Mockito.when(employeRepository.findByMatricule("M00001")).thenReturn(new Employe());

        //When/Then
        EntityExistsException e = org.junit.jupiter.api.Assertions.assertThrows(EntityExistsException.class, () -> employeService.embaucheEmploye(nom, prenom, poste, niveauEtude, tempsPartiel));
        org.junit.jupiter.api.Assertions.assertEquals("L'employé de matricule M00001 existe déjà en BDD", e.getMessage());
    }

    @Test
    public void testEmbaucheEmployeManagerMiTempsMaster99999(){
        //Given
        String nom = "Doe";
        String prenom = "John";
        Poste poste = Poste.MANAGER;
        NiveauEtude niveauEtude = NiveauEtude.MASTER;
        Double tempsPartiel = 0.5;
        Mockito.when(employeRepository.findLastMatricule()).thenReturn("99999");

        //When/Then
        EmployeException e = org.junit.jupiter.api.Assertions.assertThrows(EmployeException.class, () -> employeService.embaucheEmploye(nom, prenom, poste, niveauEtude, tempsPartiel));
        org.junit.jupiter.api.Assertions.assertEquals("Limite des 100000 matricules atteinte !", e.getMessage());
    }

    @Test
    public void testCalculSalaireMoyenETP() throws Exception {
        //Given
        Mockito.when(employeRepository.sumSalaire()).thenReturn(10000d);
        Mockito.when(employeRepository.count()).thenReturn(10L);
        Mockito.when(employeRepository.sumTempsPartiel()).thenReturn(1d);

        //When
        Double salaireMoyen = employeService.calculSalaireMoyenETP();

        //Then
        Assertions.assertThat(salaireMoyen).isEqualTo(10000d);
    }

    @Test
    public void testCalculSalaireMoyenETPBaseVide() {
        //Given
        Mockito.when(employeRepository.count()).thenReturn(0L);
        //When
        Throwable exception = Assertions.catchThrowable(() ->
                employeService.calculSalaireMoyenETP());
        // Then
        Assertions.assertThat(exception).isInstanceOf(Exception.class);
        Assertions.assertThat(exception.getMessage()).isEqualTo("Aucun employé, impossible de calculer le salaire moyen !");
    }

    @Test
    public void calculPerformanceCommercialCaTraiteNullTest() throws EmployeException {
        //Given
        String matricule = "C12345";
        Long caTraite = null;
        Long objectifCa = null;

        //When/Then
        EmployeException e = org.junit.jupiter.api.Assertions.assertThrows(EmployeException.class, () -> employeService.calculPerformanceCommercial(matricule, caTraite, objectifCa));

        org.junit.jupiter.api.Assertions.assertEquals("Le chiffre d'affaire traité ne peut être négatif ou null !", e.getMessage());
    }
}
