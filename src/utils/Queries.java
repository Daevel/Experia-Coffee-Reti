package utils;

public class Queries {
    /**
     * @description Query di visualizzazione lista database
     */
    public static final String QUERY_SHOW_DATABASES_LIKE = "SHOW DATABASES LIKE '%s'";

    /**
     * @description Query di creazione database experia_coffee_reti
     */
    public static final String QUERY_CREATE_DB = "CREATE DATABASE IF NOT EXISTS %s";

    /**
     * @description Usa il db specificato se esiste
     */
    public static final String USE_DB = "USE %s";

    /**
     * @description Query select generica
     */
    public static final String GENERIC_QUERY_SELECT = "SELECT * FROM %s";

    /**
     * @description Query di creazione tabella tbl_dipendente
     */
    public static final String TBL_EMPLOYEE_CREATE_QUERY_TABLE = "CREATE TABLE IF NOT EXISTS Dipendente (\n" +
            "    ID INT AUTO_INCREMENT PRIMARY KEY,\n" +
            "    NOME VARCHAR(50) NOT NULL,\n" +
            "    COGNOME VARCHAR(50) NOT NULL,\n" +
            "    EMAIL VARCHAR(50) NOT NULL,\n" +
            "    VIA VARCHAR(50) NOT NULL,\n" +
            "    N_CIVICO VARCHAR(50) NOT NULL,\n" +
            "    CAP VARCHAR(50) NOT NULL,\n" +
            "    CITTA VARCHAR(50) NOT NULL,\n" +
            "    CODICE_FISCALE VARCHAR(50) NOT NULL,\n" +
            "    DATA_DI_NASCITA VARCHAR(50) NOT NULL,\n" +
            "    DATA_ASSUNZIONE DATE DEFAULT NULL,\n" +
            "    PAGA_ORARIA FLOAT NOT NULL,\n" +
            "    CODICE_ZONA VARCHAR(50) DEFAULT NULL,\n" +
            "    RUOLO VARCHAR(50) DEFAULT 'dipendente',\n" +
            "    UTENTE_PASSWORD VARCHAR(50) DEFAULT '1',\n" +
            "    FOREIGN KEY (CODICE_ZONA) REFERENCES Filiale (CODICE_ZONA_FILIALE)\n" +
            "    );";

    /**
     * @description Query di creazione tabella tbl_ticketing
     */
    public static final String TBL_TICKETING_CREATE_QUERY_TABLE = "CREATE TABLE IF NOT EXISTS Ticketing (\n" +
            "    ID INT AUTO_INCREMENT PRIMARY KEY,\n" +
            "    TITOLO VARCHAR(50) NOT NULL,\n" +
            "    DESCRIZIONE VARCHAR(255) NOT NULL,\n" +
            "    GESTITO_DA VARCHAR(255) DEFAULT NULL,\n" +
            "    CREATO_DA VARCHAR(50) NULL,\n" +
            "    DATA_CREAZIONE DATE NOT NULL,\n" +
            "    STATO VARCHAR(50) DEFAULT 'Non gestito'\n" +
            ");";

    public static final String TBL_TICKETING_SELECT_STATUSES_QUERY = "SELECT ID,STATO FROM Ticketing;";

    public static final String TBL_TICKETING_DELETE_TICKET_BY_ID_QUERY = "DELETE FROM" + Constants.TBL_TICKETING + " WHERE ID = ?";

    public static final String TBL_TICKETING_INSERT_NEW_TICKET_BY_QUERY = "INSERT INTO " + Constants.TBL_TICKETING + " (TITOLO, DESCRIZIONE, STATO, DATA_CREAZIONE) VALUES (?, ?, ?, ?)";

    public static final String TBL_TICKETING_UPDATE_TICKET_STATUS_BY_QUERY = "UPDATE " + Constants.TBL_TICKETING + " SET STATO = ? WHERE ID = ?";


    //! da controllare
    public static final String TBL_MAGAZZINO_INSERT_NEW_PRODUCT_QUERY = "INSERT INTO " + Constants.TBL_MAGAZZINO + " (CODICE_MAGAZZINO, ID_PRODOTTO, QUANTITA_PRODOTTO, NOME_PRODOTTO, NOME_MAGAZZINO) VALUES (?,?,?,?,?)";

    public static final String TBL_MAGAZZINO_UPDATE_PRODUCT_QUANTITY_QUERY = "UPDATE " + Constants.TBL_MAGAZZINO + " SET QUANTITA_PRODOTTO = ? WHERE ID_PRODOTTO = ?";

    public static final String TBL_MAGAZZINO_DELETE_PRODUCT_QUERY = "DELETE FROM " + Constants.TBL_MAGAZZINO + " WHERE ID_PRODOTTO = ?";

    /**
     * @description Query di creazione tabella Ordine
     */
    public static final String TBL_ORDER_CREATE_QUERY_TABLE = "CREATE TABLE IF NOT EXISTS Ordine (\n" +
            "    ID INT AUTO_INCREMENT PRIMARY KEY,\n" +
            "    FATTURA VARCHAR(50) NOT NULL,\n" +
            "    NUMERO_ORDINE INT DEFAULT NULL,\n" +
            "    ID_CARRELLO INT NOT NULL,\n" +
            "    INDIRIZZO_SPEDIZIONE VARCHAR(50) DEFAULT NULL,\n" +
            "    STATO_ORDINE VARCHAR(50) DEFAULT 'non gestito');";

    public static final String TBL_ORDER_INSERT_QUERY_TABLE = "INSERT INTO Ordine (FATTURA, NUMERO_ORDINE, ID_CARRELLO, INDIRIZZO_SPEDIZIONE, STATO_ORDINE) VALUES\n" +
            "('FATTURA001', 1001, 101, 'Via Roma 10, Milano', 'spedito'),\n" +
            "('FATTURA002', 1002, 102, 'Via Torino 21, Roma', 'in lavorazione'),\n" +
            "('FATTURA003', 1003, 103, 'Via Firenze 5, Firenze', 'spedito'),\n" +
            "('FATTURA004', 1004, 104, 'Corso Venezia 12, Napoli', 'annullato'),\n" +
            "('FATTURA005', 1005, 105, 'Piazza Duomo 1, Milano', 'non gestito'),\n" +
            "('FATTURA006', 1006, 106, 'Via Garibaldi 7, Torino', 'spedito'),\n" +
            "('FATTURA007', 1007, 107, 'Via Verdi 9, Bologna', 'in lavorazione'),\n" +
            "('FATTURA008', 1008, 108, 'Via Mazzini 11, Genova', 'spedito'),\n" +
            "('FATTURA009', 1009, 109, 'Piazza San Marco 3, Venezia', 'annullato'),\n" +
            "('FATTURA010', 1010, 110, 'Corso Italia 2, Bari', 'spedito'),\n" +
            "('FATTURA011', 1011, 111, 'Via Dante 14, Palermo', 'in lavorazione'),\n" +
            "('FATTURA012', 1012, 112, 'Via Leopardi 6, Catania', 'non gestito'),\n" +
            "('FATTURA013', 1013, 113, 'Via Manzoni 8, Verona', 'spedito'),\n" +
            "('FATTURA014', 1014, 114, 'Piazza Navona 4, Roma', 'spedito'),\n" +
            "('FATTURA015', 1015, 115, 'Via Colombo 16, Pisa', 'in lavorazione'),\n" +
            "('FATTURA016', 1016, 116, 'Via delle Rose 20, Firenze', 'annullato'),\n" +
            "('FATTURA017', 1017, 117, 'Via Montenapoleone 3, Milano', 'spedito'),\n" +
            "('FATTURA018', 1018, 118, 'Via Roma 15, Torino', 'non gestito'),\n" +
            "('FATTURA019', 1019, 119, 'Via della Libertà 22, Palermo', 'spedito'),\n" +
            "('FATTURA020', 1020, 120, 'Piazza della Signoria 5, Firenze', 'in lavorazione');\n";

    public static final String TBL_ORDER_UPDATE_ORDER = "UPDATE " + Constants.TBL_ORDINE + " SET STATO_ORDINE = ? WHERE ID = ?";

    /**
     * @description Query di creazione tabella Magazzino
     */
    public static final String TBL_MAGAZZINO_CREATE_QUERY_TABLE = "CREATE TABLE IF NOT EXISTS Magazzino (\n" +
            "    CODICE_MAGAZZINO VARCHAR(50) NOT NULL,\n" +
            "    ID_PRODOTTO VARCHAR(50) NOT NULL,\n" +
            "    QUANTITA_PRODOTTO INT NOT NULL,\n" +
            "    NOME_PRODOTTO VARCHAR(50) DEFAULT NULL,\n" +
            "    NOME_MAGAZZINO VARCHAR(50) DEFAULT NULL,\n" +
            "    FOREIGN KEY (CODICE_MAGAZZINO) REFERENCES Filiale(CODICE_ZONA_FILIALE)\n" +
            ");";

    public static final String TBL_MAGAZZINO_INSERT_QUERY_TABLE = "INSERT INTO Magazzino (CODICE_MAGAZZINO, ID_PRODOTTO, QUANTITA_PRODOTTO, NOME_PRODOTTO, NOME_MAGAZZINO)\n" +
            "VALUES\n" +
            "    ('Z003', 'LVZ-BCEG', 30, 'Box Crema e Gusto', 'Experia Coffee Warehouse - Milan'),\n" +
            "    ('Z003', 'LVZ-BGE', 30, 'Box Gran Espresso', 'Experia Coffee Warehouse - Milan'),\n" +
            "    ('Z003', 'LVZ-BSC', 30, 'Box Super Crema', 'Experia Coffee Warehouse - Milan'),\n" +
            "    ('Z003', 'LVZ-DDC', 30, 'Box Dek Decaffeinato', 'Experia Coffee Warehouse - Milan'),\n" +
            "    ('Z003', 'LVZ-QRED', 30, 'Box Qualità Rossa', 'Experia Coffee Warehouse - Milan'),\n" +
            "    ('Z001', 'KMB-BTF', 20, 'Box Top Flavour', 'Experia Coffee Warehouse - Naples'),\n" +
            "    ('Z001', 'KMB-BPR', 20, 'Box Prestige', 'Experia Coffee Warehouse - Naples'),\n" +
            "    ('Z001', 'KMB-BNP', 20, 'Box Napoli', 'Experia Coffee Warehouse - Naples'),\n" +
            "    ('Z001', 'KMB-BGOLD', 20, 'Box Aroma Gold', 'Experia Coffee Warehouse - Naples'),\n" +
            "    ('Z001', 'KMB-BEX', 20, 'Box Extra Cream', 'Experia Coffee Warehouse - Naples'),\n" +
            "    ('Z004', 'JM-BVB', 25, 'Box Vienna Blend', 'Experia Coffee Warehouse - Florence'),\n" +
            "    ('Z004', 'JM-BRYL', 25, 'Box Vienna Blend', 'Experia Coffee Warehouse - Florence'),\n" +
            "    ('Z004', 'JM-BPLT', 25, 'Box Platinum', 'Experia Coffee Warehouse - Florence'),\n" +
            "    ('Z004', 'JM-BJB', 25, 'Box Jubilee Blend', 'Experia Coffee Warehouse - Florence'),\n" +
            "    ('Z004', 'JM-BAS', 25, 'Box Amabili Sensi', 'Experia Coffee Warehouse - Florence'),\n" +
            "    ('Z002', 'ILY-BIM', 20, 'Box illy Monoarabica', 'Experia Coffee Warehouse - Rome'),\n" +
            "    ('Z002', 'ILY-BII', 20, 'Box illy Intenso', 'Experia Coffee Warehouse - Rome'),\n" +
            "    ('Z002', 'ILY-BIDCF', 20, 'Box illy Decaffeinato', 'Experia Coffee Warehouse - Rome'),\n" +
            "    ('Z002', 'ILY-BIC', 20, 'Box illy Classico', 'Experia Coffee Warehouse - Rome'),\n" +
            "    ('Z002', 'ILY-BIB', 20, 'Box illy Blend', 'Experia Coffee Warehouse - Rome'),\n" +
            "    ('Z005', 'DLY-BPD', 20, 'Box Prodomo', 'Experia Coffee Warehouse - Palermo'),\n" +
            "    ('Z005', 'DLY-BMNC', 20, 'Box Monaco', 'Experia Coffee Warehouse - Palermo'),\n" +
            "    ('Z005', 'DLY-BETP', 20, 'Box Ethiopia', 'Experia Coffee Warehouse - Palermo'),\n" +
            "    ('Z005', 'DLY-BCL', 20, 'Box Classic', 'Experia Coffee Warehouse - Palermo'),\n" +
            "    ('Z005', 'DLY-BCGOLD', 20, 'Box Crema Oro', 'Experia Coffee Warehouse - Palermo');";


    /**
     * @description Query di creazione tabella Filiale
     */
    public static final String TBL_FILIALE_CREATE_QUERY_TABLE = "CREATE TABLE IF NOT EXISTS Filiale (\n" +
            "    NOME_FILIALE VARCHAR(50) NOT NULL,\n" +
            "    SEDE VARCHAR(50) NOT NULL,\n" +
            "    CODICE_ZONA_FILIALE VARCHAR(50) NOT NULL,\n" +
            "    PRIMARY KEY (CODICE_ZONA_FILIALE)\n" +
            "    );\n";

    /**
     * @description Query di inserimento record in Filiale
     */
    public static final String TBL_FILIALE_INSERT_QUERY_TABLE = "INSERT INTO Filiale (NOME_FILIALE, SEDE, CODICE_ZONA_FILIALE) \n" +
            "VALUES\n" +
            "    ('Experia Coffee - Default', 'TBD', 'Z000'),\n" +
            "    ('Experia Coffee - Naples', 'Napoli', 'Z001'),\n" +
            "    ('Experia Coffee - Rome', 'Roma', 'Z002'),\n" +
            "    ('Experia Coffee - Milan', 'Milano', 'Z003'),\n" +
            "    ('Experia Coffee - Florence', 'Firenze', 'Z004'),\n" +
            "    ('Experia Coffee - Palermo', 'Palermo', 'Z005');\n";
    /**
     * @description Query di inserimento record in Dipendente
     */
    public static final String TBL_EMPLOYEE_INSERT_QUERY_TABLE = "INSERT INTO Dipendente (ID, NOME, COGNOME, VIA, CITTA, CAP, CODICE_FISCALE, DATA_ASSUNZIONE, PAGA_ORARIA, CODICE_ZONA, EMAIL, N_CIVICO, DATA_DI_NASCITA)\n" +
            "VALUES\n" +
            "    (9, 'Fulvio', 'Amante', 'Via Benedetto Cozzolino 2', 'Napoli', '80100', 'AMTFLV123456RDF4', DATE '2023-03-03', 7.5, 'Z001', 'fulvio.amante@experiacoffee.it', '25A', '1990-01-01'),\n" +
            "    (2, 'Marco', 'Aurelio', 'Via del pantano 5', 'Roma', '0100', 'ARLMRC0231234567', DATE '2023-05-06', 7.5, 'Z002', 'marco.aurelio@experiacoffee.it', '13B', '1991-05-15'),\n" +
            "    (18, 'Salvatore', 'Amitrano', 'Via XX Maggio, 12', 'Milano', '20019', 'ATOSTE745581DF26', DATE '2023-02-02', 7.9, 'Z003', 'salvatore.amitrano@experiacoffee.it', '42C', '1985-03-15'),\n" +
            "    (11, 'Alessandro', 'Bonifacio', 'Via Magellano 18', 'Firenze', '50100', 'BNFALS2362122203', DATE '2022-10-03', 8.7, 'Z004', 'alessandro.bonifacio@experiacoffee.it', '7D', '1991-08-20'),\n" +
            "    (12, 'Giovanni', 'Chierico', 'Via I Maggio 5', 'Empoli', '50053', 'CHRGVN4571254863', DATE '2022-07-03', 7, 'Z004', 'giovanni.chierico@experiacoffee.it', '19E', '1985-07-12'),\n" +
            "    (13, 'Sofia', 'Chieti', 'Via delle Rose 4', 'Pescara', '65100', 'CHTSFA2361047895', DATE '2020-02-03', 10, 'Z002', 'sofia.chieti@experiacoffee.it', '33F', '1988-11-25'),\n" +
            "    (4, 'Isabella', 'Crispo', 'Via Madonna delle grazie 3', 'Roma', '0100', 'CRSISA123DD47521', DATE '2023-10-03', 10, 'Z002', 'isabella.crispo@experiacoffee.it', '14G', '1990-01-01'),\n" +
            "    (14, 'Francesca', 'Diamante', 'Via Benedetto Croce 2', 'Ravenna', '48100', 'DMTFRA2345120159', DATE '2023-03-03', 7.6, 'Z004', 'francesca.diamante@experiacoffee.it', '9H', '1988-10-15'),\n" +
            "    (6, 'Gennaro', 'Esposito', 'Via casacelle 100', 'Napoli', '80100', 'ESPGNN123456RDF4', DATE '2022-10-03', 7.5, 'Z001', 'gennaro.esposito@experiacoffee.it', '5I', '1991-05-15'),\n" +
            "    (10, 'Pasquale', 'Granata', 'Via Oasi Sacro Cuore 15', 'Napoli', '80100', 'ESPPQL123456RDF4', DATE '2021-10-27', 7.5, 'Z001', 'pasquale.granata@experiacoffee.it', '11L', '1989-02-18'),\n" +
            "    (19, 'Giuseppe', 'Emirati', 'Via Epitaffio, 12', 'Milano', '20019', 'ETIGPE8519556347', DATE '2023-02-02', 7.9, 'Z003', 'giuseppe.emirati@experiacoffee.it', '28M', '1990-08-30'),\n" +
            "    (5, 'Patrizio', 'Infante', 'Via Augusto Righi 17', 'Roma', '0100', 'IFTPTZ3456GG7656', DATE '2023-07-02', 8.5, 'Z002', 'patrizio.infante@experiacoffee.it', '10N', '1991-08-30'),\n" +
            "    (1, 'Aurelio', 'Lagnani', 'Via del colosseo 15', 'Roma', '0100', 'LGIARL345RF43176', DATE '2022-05-09', 7.5, 'Z002', 'aurelio.lagnani@experiacoffee.it', '8O', '1988-05-20'),\n" +
            "    (16, 'Armando', 'Laddomada', 'Giovanni Falcone', 'Palermo', '90100', 'LMAARO123ED4SA5G', DATE '2022-04-06', 8, 'Z005', 'armando.laddomada@experiacoffee.it', '17P', '1988-12-10'),\n" +
            "    (3, 'Gianluca', 'Materazzi', 'Via del gran sasso 18', 'Roma', '0100', 'MTIGNA8542336951', DATE '2023-05-06', 7.5, 'Z002', 'gianluca.materazzi@experiacoffee.it', '22Q', '1990-01-01'),\n" +
            "    (17, 'Damiano', 'Piacenti', 'Via del Sole', 'Ragusa', '97100', 'PCIDMO1249665843', DATE '2022-05-04', 7, 'Z005', 'damiano.piacenti@experiacoffee.it', '48R', '1988-06-15'),\n" +
            "    (7, 'Carmela', 'Patrizi', 'Via Pianura 10', 'Napoli', '80100', 'PTZCRA123456RDF4', DATE '2022-07-03', 7.5, 'Z001', 'carmela.patrizi@experiacoffee.it', '14S', '1991-05-15'),\n" +
            "    (20, 'Fabio', 'Salice', 'Via 2 Novembre, 25', 'Milano', '20019', 'SLEFBO1035478445', DATE '2022-10-02', 8, 'Z003', 'fabio.salice@experiacoffee.it', '37T', '1989-01-05'),\n" +
            "    (21, 'Federico', 'Senna', 'Via delle stelle, 36-B', 'Milano', '20019', 'SNAFCO8954113745', DATE '2023-04-12', 6.5, 'Z003', 'federico.senna@experiacoffee.it', '49U', '1990-01-01'),\n" +
            "    (15, 'Damiano', 'Volpe', 'Via dei fiori sospesi 15', 'Cremona', '26100', 'VLPDMA1782002345', DATE '2021-10-27', 7.2, 'Z003', 'damiano.volpe@experiacoffee.it', '12V', '1988-01-15'),\n" +
            "    (8, 'Amelia', 'Verdi', 'Via Cristoforo Colombo 17', 'Napoli', '80100', 'VRDAML123456RDF4', DATE '2020-02-03', 7.5, 'Z001', 'amelia.verdi@experiacoffee.it', '29Z', '1988-10-30');";


}
