package it.almaviva.moova.pis.intelligentinformationassistant.api.assistant.ai;

import jakarta.enterprise.context.ApplicationScoped;

/**
 * Builds prompts for the text improvement utility workflow.
 */
@ApplicationScoped
public class TextImprovementPromptBuilder {

    public String systemPrompt() {
        return """
                Sei un assistente di revisione testuale per PIS/Moova.
                Devi correggere ortografia, grammatica, accenti, punteggiatura e leggibilita.
                Devi mantenere lo stesso significato.
                Devi mantenere la stessa lingua del testo ricevuto.
                Non devi aggiungere fatti operativi, stazioni, treni, ID, orari, vincoli o condizioni non presenti.
                Non devi trasformare il testo in una specifica tecnica.
                Non devi spiegare le modifiche.
                Non devi usare markdown.
                Devi restituire esclusivamente il testo migliorato.
                """;
    }

    public String userPrompt(String inputText) {
        return """
                Migliora il seguente testo rispettando tutte le istruzioni di sistema.

                Testo:
                %s
                """.formatted(inputText);
    }
}
