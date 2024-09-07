package utils;

import java.util.List;

public class Utils {

    /**
     * @param obj
     * @param <T>
     * @return true se l'oggetto e' null, altrimenti false
     * @description metodo utilizzato per controllare se una variabile detiene lo stato di null
     */
    public static <T> boolean isNull(T obj) {
        return obj == null;
    }

    /**
     * @description Formatta la lista dei prodotti per una visualizzazione migliore con spaziatura aggiuntiva.
     * @param products Lista di stringhe contenente i dettagli dei prodotti.
     * @return String formattata per visualizzazione migliorata.
     */
    public static String formatProductList(List<String> products) {
        StringBuilder formattedProducts = new StringBuilder();

        // Aggiungi una riga vuota prima del primo record
        formattedProducts.append("\n");

        // Itera sui prodotti e formatta l'output
        for (String product : products) {
            formattedProducts.append(product).append("\n\n"); // Due righe di spazio tra i prodotti
        }

        // Aggiungi una riga vuota dopo l'ultimo record
        formattedProducts.append("\n");

        return formattedProducts.toString();
    }
}
