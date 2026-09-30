package br.com.fiap.petfiap.model;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

// Padrao Singleton (Aula 14): uma unica instancia em toda a aplicacao,
// responsavel por gerar os protocolos sequenciais dos atendimentos.
// Thread-safe para o uso concorrente do pet shop.
public class GeradorProtocolo {

    private static final Logger logger = LoggerFactory.getLogger(GeradorProtocolo.class);

    private static GeradorProtocolo instancia;

    private int contador;

    private GeradorProtocolo() {
        contador = 0;
        logger.debug("GeradorProtocolo criado");
    }

    public static synchronized GeradorProtocolo getInstancia() {
        if (instancia == null) {
            instancia = new GeradorProtocolo();
        }
        return instancia;
    }

    public synchronized int proximo() {
        contador++;
        return contador;
    }
}
