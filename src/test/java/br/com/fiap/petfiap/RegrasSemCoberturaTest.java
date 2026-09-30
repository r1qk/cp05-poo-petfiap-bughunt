package br.com.fiap.petfiap;

import br.com.fiap.petfiap.exception.StatusInvalidoException;
import br.com.fiap.petfiap.model.Atendimento;
import br.com.fiap.petfiap.model.Banho;
import br.com.fiap.petfiap.model.ConsultaVeterinaria;
import br.com.fiap.petfiap.model.Tosa;
import br.com.fiap.petfiap.repository.AtendimentoRepository;
import br.com.fiap.petfiap.service.AgendaService;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import java.time.LocalDateTime;
import java.util.Optional;
import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
public class RegrasSemCoberturaTest {
    @Mock
    private AtendimentoRepository repository;
    @InjectMocks
    private AgendaService service;

    @Test
    public void deveCobrarPrecoCorrespondenteQuandoPorteDoBanhoVariar() {
        // Arrange
        LocalDateTime data = LocalDateTime.now().plusDays(1);
        Banho pequeno = new Banho(1, "Luna", "PEQUENO", "Ana", data);
        Banho medio = new Banho(2, "Luna", "MEDIO", "Ana", data);
        Banho grande = new Banho(3, "Luna", "GRANDE", "Ana", data);
        // Act
        double precoPequeno = pequeno.calcularPreco();
        double precoMedio = medio.calcularPreco();
        double precoGrande = grande.calcularPreco();
        // Assert
        assertAll(() -> assertEquals(60.0, precoPequeno, 0.001),
                () -> assertEquals(80.0, precoMedio, 0.001),
                () -> assertEquals(100.0, precoGrande, 0.001));
    }


    @Test
    public void deveDurar60MinutosQuandoAtendimentoForTosa() {
        // Arrange
        Atendimento tosa = new Tosa(1, "Luna", "MEDIO", "Ana", LocalDateTime.now().plusDays(1));
        // Act
        int duracao = tosa.getDuracaoMinutos();
        // Assert
        assertEquals(60, duracao);
    }


    @Test
    public void deveRecusarCancelamentoQuandoAtendimentoJaEstiverConcluido() {
        // Arrange
        Banho banho = new Banho(1, "Luna", "MEDIO", "Ana", LocalDateTime.now().plusDays(1));
        banho.concluir();
        when(repository.findById(1L)).thenReturn(Optional.of(banho));
        // Act
        assertThrows(StatusInvalidoException.class, () -> service.cancelar(1L));
        // Assert
        assertEquals("CONCLUIDO", banho.getStatus());
        verify(repository, never()).save(any());
    }


    @Test
    public void deveRecusarAgendamentoQuandoDataHoraEstiverNoPassado() {
        // Arrange
        Banho banho = new Banho(1, "Luna", "MEDIO", "Ana", LocalDateTime.now().minusDays(1));
        // Act
        assertThrows(IllegalArgumentException.class, () -> service.agendar(banho));
        // Assert
        verifyNoInteractions(repository);
        assertEquals("AGENDADO", banho.getStatus());
    }


    @Test
    public void deveCustar150ReaisQuandoPorteDaConsultaVariar() {
        // Arrange
        LocalDateTime data = LocalDateTime.now().plusDays(1);
        String[] portes = {"PEQUENO", "MEDIO", "GRANDE"};
        for (String porte : portes) {
            ConsultaVeterinaria consulta = new ConsultaVeterinaria(1, "Luna", porte, "Ana", data);
            // Act
            double preco = consulta.calcularPreco();
            // Assert
            assertEquals(150.0, preco, 0.001, "Porte: " + porte);
        }
    }


    @Test
    public void deveRecusarConclusaoQuandoAtendimentoEstiverCancelado() {
        // Arrange
        Banho banho = new Banho(1, "Luna", "MEDIO", "Ana", LocalDateTime.now().plusDays(1));
        banho.cancelar();
        when(repository.findById(1L)).thenReturn(Optional.of(banho));
        // Act
        assertThrows(StatusInvalidoException.class, () -> service.concluir(1L));
        // Assert
        assertEquals("CANCELADO", banho.getStatus());
        verify(repository, never()).save(any());
    }

}
