package dp;

import java.io.File;
import java.io.FileNotFoundException;
import java.io.FileReader;
import java.io.FileWriter;
import java.io.IOException;
import java.security.SecureRandom;
import java.util.ArrayList;
import java.util.HashSet;
import java.util.Iterator;
import java.util.Scanner;

/**
 *
 * @author Tarcisio Lucas
 */
public class D {

    public static String baseName;
    public static String path;
    public static int examplesNumber;
    public static int numeroExemplosPositivo;    //
    public static int numeroExemplosNegativo;    // 
    public static int attributesNumber;
    public static int numeroItens;

    public static String SEPARADOR = ",";

    public static String[] attributeNames;

    public static int[] itemAtributo;
    public static int[] itemValor;
    public static String[] itemAtributoStr;
    public static String[] itemValorStr;

    public static int[][] Dp;     //
    public static int[][] Dn;     //

    public static int[] itensUtilizados;
    public static int numeroItensUtilizados;

    public static final int TIPO_CSV = 0;
    public static final int TIPO_ARFF = 1;
    public static final int TIPO_EXCEL = 2;

    public static String[][] examplesMatrix;
    public static String[][][] partitions;        // an 1D array of 2D arrays
    public static int numberOfPartitions;
    public static ArrayList<HashSet<String>> valoresDistintosAtributos;

    public static String tipoDiscretizacao;

    public static String targetValue = "";
    public static String[] targetValues;

    /**
     * Recebe caminho para base de dados e tipo de formato e carrega base de dados na classe D
     *
     * @param caminho - caminho do arquivo completo
     * @param tipoArquivo - tipodo arquivo: CSV, ARFF, EXCEL, etc.
     * @throws FileNotFoundException
     */
    /*
    public static void CarregarArquivo_old(String path, int tipoArquivo) throws FileNotFoundException{
          
        //Passa dados do formato específico para um formato padrão definido por nós: String[][] dataRowsMatrix 
        D.dataRowsMatrix = null;
        switch(tipoArquivo){
            case D.TIPO_CSV:
                dataRowsMatrix = D.csvToExamplesStringMatrix(path);
                break;
            case D.TIPO_ARFF:
                //não implementado
                break;
            case D.TIPO_EXCEL:
                //não implementado
                break;
        }            
        
        //Carrega a partir do nosso formato em D
        D.generateItens(dataRowsMatrix); 
        
        //Filtro determina os itens que serão considerados pelos algoritmos
        //Por padrão todos são aceitos
        D.numeroItensUtilizados = D.numeroItens;
        D.itensUtilizados = new int[D.numeroItensUtilizados];
        for(int l = 0; l < D.numeroItensUtilizados; l++){
            D.itensUtilizados[l] = l;
        }           
    }
     */
    /**
     * Recebe path para base de dados e tipo de formato e carrega base de dados na classe D
     *
     * @param caminho - path do arquivo completo
     * @param tipoArquivo - tipo do arquivo: CSV, ARFF, EXCEL, etc.
     * @throws FileNotFoundException
     */
    public static void loadFile(String caminho, int tipoArquivo) throws FileNotFoundException {

        //Passa dados do formato específico para um formato padrão definido por nós: String[][] dataRowsMatrix 
        D.examplesMatrix = null;
        switch (tipoArquivo) {
            case D.TIPO_CSV:
                D.examplesMatrix = D.csvToExamplesStringMatrix(caminho);
                break;
            case D.TIPO_ARFF:
                //não implementado
                break;
            case D.TIPO_EXCEL:
                //não implementado
                break;
        }
    }

    //Densidade é a quantidade
    public static double densidade() {
        return 0.0;
    }

    /**
     * Recebe caminho para arquivo .CSV ou .csv e retorna matriz de strings na qual dadosStr:String[numeroExemplo][numeroAtributos]. Além disso: (1) Salva nome da base em D.nomeBase (2) Salva os nomes dos atributos e do rótulo em D.nomeVariaveis (3) Salva número de exemplos e de atributos (4) Salva caminho da base em D.caminho
     *
     * @param caminho
     * @return String[][] - String[numeroExemplo][attributesNumber]
     * @throws FileNotFoundException
     */
    private static String[][] csvToExamplesStringMatrix(String caminho) throws FileNotFoundException {
        D.path = caminho;
        Scanner scanner = new Scanner(new FileReader(D.path)).useDelimiter("\\n");          // Reading file in standard format
        ArrayList<String[]> dataRows = new ArrayList<>();                                   // ArrayList to store the rows of the dataset        

        String[] pathComponents = D.path.split("\\\\");
        if (pathComponents.length == 1) {
            pathComponents = D.path.split("/");                                          // Caso separador de pastas seja / e  não \\
        }

        // Defining the base name
        D.baseName = pathComponents[pathComponents.length - 1].replace(".CSV", "");           // Base name is the last word (case .CSV)
        D.baseName = D.baseName.replace(".csv", "");                                        //(case .csv)

        // Initializing the names and the number of attributes
        D.attributeNames = scanner.next().split(D.SEPARADOR);                               // 1º line: variable names (including the label)
        D.attributesNumber = D.attributeNames.length - 1;                                   // number of variables (excluding the label)
        for (int i = 0; i < D.attributeNames.length; i++) {
            D.attributeNames[i] = D.attributeNames[i].replaceAll("[\"\r\']", "");           // Clearing attribute names (removing the double quotes)
        }

        while (scanner.hasNext()) {
            dataRows.add(scanner.next().split(D.SEPARADOR));
        }

        D.examplesNumber = dataRows.size();

        //HashSet<String> targetValuesHashSet = new HashSet<String>();
        String[][] dataRowsMatrix = new String[D.examplesNumber][D.attributesNumber + 1];
        for (int i = 0; i < dataRows.size(); i++) {
            String[] row = dataRows.get(i);                                  // gets a data line
            for (int j = 0; j < row.length; j++) {
                dataRowsMatrix[i][j] = row[j].replaceAll("[\"\r\']", "");    // Removing the double quotes
            }
            //valoresAlvoHasSet.add(row[D.attributesNumber]);
            //targetValuesHashSet.add(dataRowsMatrix[i][D.attributesNumber]);
        }

        /* Still checking if it's useful...
        //Coletanto valores distintos do atributo alvo  
        D.targetValues = new String[targetValuesHashSet.size()];
        Iterator iterator = targetValuesHashSet.iterator();
        int indice = 0;
        while (iterator.hasNext()) {
            D.targetValues[indice++] = (String) iterator.next();
        }
        Arrays.sort(D.targetValues);
         */
        return dataRowsMatrix;
    }

    /**
     * Recebe uma matriz contendo todas as linhas da base e retorna uma amostra de exemplos aleatórios. Além disso:
     *
     * @param dataRowsString
     * @return dataSampleString :ArrayList<String[]>
     */
    private static String[][] randomSampling(double samplingRate) {
        SecureRandom random = new SecureRandom();
        random.setSeed(Const.SEEDS[0]);
        int sampleSize = (int) (samplingRate * D.examplesNumber);
        String[][] dataSample = new String[sampleSize][D.attributesNumber + 1];
        String[] randomRow;

        int i = 0;
        while (i < sampleSize) {
            randomRow = D.examplesMatrix[(random.nextInt(D.examplesNumber))];
            if (containsRow(dataSample, randomRow)) {
                continue;
            }
            dataSample[i] = randomRow;
            i++;
        }
        return dataSample;
    }

    public static void createPartitions(int partitionsNumber) {
        //Capturar número de exemplos positivos (y="p") e negativos (y="n")
        int indiceRotulo = D.attributesNumber;
        int countP = 0 , countN = 0;
        for (int i = 0; i < D.examplesNumber; i++) {
            String y = D.examplesMatrix[i][indiceRotulo];
            if (y.equals("p")) {
                countP++;
            } else {
                countN++;
            }
        }
        int positivesEachPartition = countP/partitionsNumber;
        
        String[][] positiveExamples = new String[countP][];
        String[][] negativeExamples = new String[countN][];
       
        int pIndex =0, nIndex =0;
        for (int i = 0; i < D.examplesMatrix.length; i++) {
            String y = D.examplesMatrix[i][indiceRotulo];
            if (y.equals("p")) {
                positiveExamples[pIndex] = D.examplesMatrix[i];
                pIndex++;
            } else {
                negativeExamples[nIndex] = D.examplesMatrix[i];
                nIndex++;
            }
        }
        
        D.numberOfPartitions = partitionsNumber;
        D.partitions = new String[partitionsNumber + 1][][];
        D.partitions[0] = D.examplesMatrix;
        
        for (int i = 1; i < D.partitions.length; i++) {
            D.partitions[i] = D.randomSampling(0.4);
        }
    }
    
    //-------------------------------------------------------------------------------------------------------------------
    public static void createOnePartition(double totalSampleRate, double samplingRate) {
        D.numberOfPartitions = 1;
        D.partitions = new String[2][][];
        D.partitions[0] = D.examplesMatrix;
        //D.partitions[1] = D.randomSampling(samplingRate);
        D.partitions[1] = D.getSampledMatrix(totalSampleRate, samplingRate);
    }
    
    private static String[][] getSampledMatrix(double totalSampleRate, double positiveSamplingRate){
        
        int sampleSize = (int) (totalSampleRate * D.examplesNumber);
        int expectedPositiveQty = (int) (sampleSize * positiveSamplingRate);
        int expectedNegativeQty = sampleSize - expectedPositiveQty;
       
        //Capturar número de exemplos positivos (y="p") e negativos (y="n")
        int indiceRotulo = D.attributesNumber;
        int countP = 0 , countN = 0;
        for (int i = 0; i < D.examplesNumber; i++) {
            String y = D.examplesMatrix[i][indiceRotulo];
            if (y.equals("p")) {
                countP++;
            } else {
                countN++;
            }
        }
        
        if(countP < expectedPositiveQty ){
            System.out.println("\nWARNING !");
            System.out.println("Número de exemplos positivos insuficiente para alcançar a porcentagem solicitada:");
            System.out.println("Número de exemplos positivos na base: "+ countP);
            System.out.println("Número de exemplos positivos necessário: " + expectedPositiveQty);
            expectedPositiveQty = countP;
            expectedNegativeQty = sampleSize - expectedPositiveQty;
        }else if(countN < expectedNegativeQty){
            System.out.println("\nWARNING !");
            System.out.println("Número de exemplos negativos insuficiente para alcançar a porcentagem solicitada:");
            System.out.println("Número de exemplos negativos na base: "+ countN);
            System.out.println("Número de exemplos negativos necessário: " + expectedNegativeQty);
             expectedNegativeQty = countN;
             expectedPositiveQty = sampleSize - expectedNegativeQty;
        }
        
        String[][] positiveExamples = new String[countP][];
        String[][] negativeExamples = new String[countN][];
       
        int pIndex =0, nIndex =0;
        for (int i = 0; i < D.examplesMatrix.length; i++) {
            String y = D.examplesMatrix[i][indiceRotulo];
            if (y.equals("p")) {
                positiveExamples[pIndex] = D.examplesMatrix[i];
                pIndex++;
            } else {
                negativeExamples[nIndex] = D.examplesMatrix[i];
                nIndex++;
            }
        }
        positiveExamples = D.randomSampling(positiveExamples, expectedPositiveQty);
        negativeExamples = D.randomSampling(negativeExamples, expectedNegativeQty);
        String[][] resultExamplesMatrix =  new String[positiveExamples.length + negativeExamples.length][];
        int i;
        for(i = 0; i < positiveExamples.length; i++){
            resultExamplesMatrix[i] = positiveExamples[i];
        }
        for(int j = 0; j <negativeExamples.length; j++){
            resultExamplesMatrix[i++] = negativeExamples[j];
        }
        System.out.println("\nNÚMERO DE EXEMPLOS NEGATIVOS NA AMOSTRA: "+ negativeExamples.length);
        System.out.println("NÚMERO DE EXEMPLOS POSTIVOS NA AMOSTRA: "+ positiveExamples.length);
        System.out.println("TAMNHO DA AMOSTRA: "+ resultExamplesMatrix.length);
        return resultExamplesMatrix;
    }
    //-------------------------------------------------------------------------------------------------------------------
    
    private static String[][] randomSampling(String[][] matrix, int sampleSize) {
        SecureRandom random = new SecureRandom();
        random.setSeed(Const.SEEDS[0]);
        String[][] dataSample = new String[sampleSize][matrix[0].length];
        String[] randomRow;
        
        int i = 0;
        while (i < sampleSize) {
            randomRow = matrix[(random.nextInt(matrix.length))];
            if (containsRow(dataSample, randomRow)) {
                continue;
            }
            dataSample[i] = randomRow;
            i++;
        }
        return dataSample;
    }
   
    public static void switchPartition(int partitionIndex) {
        D.examplesMatrix = D.partitions[partitionIndex];
        D.examplesNumber = D.examplesMatrix.length;
        int[][] dadosInt = generateExamplesIntMatrix();
        D.generateDpDn(dadosInt); //Gera Bases de exemplos positivos (D+) e negativos (D-)

    }

    private static boolean containsRow(String[][] matrix, String[] row) {
        for (String[] matrixRow : matrix) {
            if (matrixRow == row) {
                return true;
            }
        }
        return false;

    }

    /*private static ArrayList<String[]> stratifiedSampling(ArrayList<String[]> dataRowsString, double positiveExamplesRate){
        //Capturar número de exemplos positivos (y="p") e negativos (y="n")
        int indiceRotulo = D.attributesNumber;
        D.numeroExemplosPositivo = 0;
        D.numeroExemplosNegativo = 0;
        //Contanto número de exemplos positivos e negativos
        for(int i = 0; i < D.examplesNumber; i++){
            String y = D.examplesMatrix[i][indiceRotulo];
            //if(y.equals(D.targetValue) || y.equals("\"" + D.targetValue + "\"\r") || y.equals("\'" + D.targetValue + "\'\r") || y.equals(D.targetValue + "\r")){
            if(y.equals(D.targetValue)){
                D.numeroExemplosPositivo++;
            }else{
                D.numeroExemplosNegativo++;
            }
        }
        return null;
    }*/
    /**
     * Gera D, Dp, Dn e itens a partir da base salva no formato de matriz de String: dataRowsMatrix: String[][]
     *
     * @param label: String valor de referência para dividir Dp e Dn
     * @throws java.io.FileNotFoundException
     */
    public static void setup(String label) throws FileNotFoundException {
        //Atribuindo alvo
        D.targetValue = label;
        D.valoresDistintosAtributos = generateDistinctAttributeValues();
        //Carrega a partir do nosso formato em D
        D.generateItens();

        if (D.numberOfPartitions > 0) {
            D.switchPartition(1);
        }else{
            int[][] dadosInt = generateExamplesIntMatrix();
            D.generateDpDn(dadosInt); //Gera Bases de exemplos positivos (D+) e negativos (D-)
        }

        //Filtro determina os itens que serão considerados pelos algoritmos
        //Por padrão todos são aceitos
        D.numeroItensUtilizados = D.numeroItens;
        D.itensUtilizados = new int[D.numeroItensUtilizados];
        for (int l = 0; l < D.numeroItensUtilizados; l++) {
            D.itensUtilizados[l] = l;
        }
    }

    private static ArrayList<HashSet<String>> generateDistinctAttributeValues() {
        //Capturando os valores distintos de cada atributo
        ArrayList<HashSet<String>> valoresDistintosAtributosLocal = new ArrayList<>(); //Amazena os valores distintos de cada atributo em um linha
        D.numeroItens = 0;
        for (int i = 0; i < D.attributesNumber; i++) {
            HashSet<String> valoresDistintosAtributo = new HashSet<>(); //Armazena valores distintos de apenas um atributo. Criar HashSet para armezenar valores distintos de um atributo. Não admite valores repetidos!
            for (int j = 0; j < D.examplesNumber; j++) {
                valoresDistintosAtributo.add(D.examplesMatrix[j][i]); //Coleção não admite valores repetidos a baixo custo computacional.
            }
            D.numeroItens += valoresDistintosAtributo.size();

            valoresDistintosAtributosLocal.add(valoresDistintosAtributo); //Adiciona lista de valores distintos do atributo de índice i na posição i do atributo atributosEvalores
        }
        return valoresDistintosAtributosLocal;
    }

    /**
     * Recebe dados no formato de String e preenche classe D com o universo de itens e exemplos positivos e negativos (1) Gera universos de itens (atributo, valores) carregando em itemAtributoStr(String[]) e itemValorStr(String[]) (2) Mapeia universo de itens no formato original para inteiros: itemAtributo(int[]) e itemValor(int[]) (3) Mapeia base de dados para o formato de inteiros OBS: a posição do array é o Item no problema de Grupos Discriminativos. Posição i, por exemplo é um item que representa o atributo itemAtributoStr[i] com valor itemValorStr[i]. Tais valores são mapeados nos inteiros itemAtributo[i] e itemValor[i], formato final da base de dados utilizadas pelos algoritmos.
     *
     * @param dataStringMatrix
     */
    private static void generateItens() {

        //Gera 4 arrays para armazenar o universo de atributos e valores no formato original (String) e mapeado para inteiro.
        D.itemAtributoStr = new String[D.numeroItens];
        D.itemValorStr = new String[D.numeroItens];
        D.itemAtributo = new int[D.numeroItens];
        D.itemValor = new int[D.numeroItens];

        //Carrega arrays com universos de itens com valores reais e respectivos inteiros mapeados
        int[][] dadosInt; //dados no formato inteiro: mais rápido comparar inteiros que strings
        int indiceItem = 0; //Indice vai de zero ao número de itens total
        for (int indiceAtributo = 0; indiceAtributo < D.valoresDistintosAtributos.size(); indiceAtributo++) {
            Iterator valoresDistintosAtributoIterator = D.valoresDistintosAtributos.get(indiceAtributo).iterator(); //Capturando valores distintos do atributo de indice i
            int indiceValor = 0; //vai mapear um inteiro distinto para cada valor distinto de cada variável

            //Para cada atributo: 
            //Atribui inteiro para atributo e a cada valor do atributo.  
            //Realizar mapeamento na matriz de dados no formato inteiro
            while (valoresDistintosAtributoIterator.hasNext()) {
                D.itemAtributoStr[indiceItem] = D.attributeNames[indiceAtributo]; //
                D.itemValorStr[indiceItem] = (String) valoresDistintosAtributoIterator.next();
                D.itemAtributo[indiceItem] = indiceAtributo;
                D.itemValor[indiceItem] = indiceValor;

                indiceValor++;
                indiceItem++;
            }
        }

    }

    private static int[][] generateExamplesIntMatrix() {
        int[][] dadosInt = new int[D.examplesNumber][D.attributesNumber]; //dados no formato inteiro: mais rápido comparar inteiros que strings
        int indiceItem = 0; //Indice vai de zero ao número de itens total
        int indiceValor;
        /*for (int indiceAtributo = 0; indiceAtributo < D.attributesNumber; indiceAtributo++) {
            
                for (int m = 0; m < D.examplesNumber; m++) {
                    if (D.examplesMatrix[m][indiceAtributo].equals(D.itemValorStr[indiceItem])) {
                        dadosInt[m][indiceAtributo] = D.itemValor[indiceItem];
                    }
                }
                indiceItem++;
        }*/
        for (int indiceAtributo = 0; indiceAtributo < D.valoresDistintosAtributos.size(); indiceAtributo++) {
            Iterator valoresDistintosAtributoIterator = D.valoresDistintosAtributos.get(indiceAtributo).iterator(); //Capturando valores distintos do atributo de indice i
            indiceValor = 0; //vai mapear um inteiro distinto para cada valor distinto de cada variável
            while (valoresDistintosAtributoIterator.hasNext()) {
                valoresDistintosAtributoIterator.next();
                //Preenche respectivo item (atributo, Valor) na matrix dadosInt com inteiro que mapeia valor categórico da base
                for (int m = 0; m < D.examplesNumber; m++) {
                    if (D.examplesMatrix[m][indiceAtributo].equals(D.itemValorStr[indiceItem])) {
                        dadosInt[m][indiceAtributo] = D.itemValor[indiceItem];
                    }
                }
                indiceValor++;
                indiceItem++;
            }
        }

        return dadosInt;
    }

    /**
     * Gerar bases D+ (ou Dp) e D- (ou Dn) no formato numérico considerando D.targetValue como classe alvo
     *
     * @param dataStringMatrix
     * @param dadosInt
     */
    private static void generateDpDn(int[][] dadosInt) {
        //Capturar número de exemplos positivos (y="p") e negativos (y="n")
        int indiceRotulo = D.attributesNumber;
        D.numeroExemplosPositivo = 0;
        D.numeroExemplosNegativo = 0;
        //Contanto número de exemplos positivos e negativos
        for (int i = 0; i < D.examplesNumber; i++) {
            String y = D.examplesMatrix[i][indiceRotulo];
            //if(y.equals(D.targetValue) || y.equals("\"" + D.targetValue + "\"\r") || y.equals("\'" + D.targetValue + "\'\r") || y.equals(D.targetValue + "\r")){
            if (y.equals(D.targetValue)) {
                D.numeroExemplosPositivo++;
            } else {
                D.numeroExemplosNegativo++;
            }
        }

        //inicializando Dp e Dn
        D.Dp = new int[D.numeroExemplosPositivo][D.attributesNumber];
        D.Dn = new int[D.numeroExemplosNegativo][D.attributesNumber];

        int indiceDp = 0;
        int indiceDn = 0;
        for (int i = 0; i < D.examplesNumber; i++) {
            String yValue = D.examplesMatrix[i][indiceRotulo];
            //if(yValue.equals(D.targetValue) || yValue.equals("\"" + D.targetValue + "\"\r") || yValue.equals("\'" + D.targetValue + "\'\r") || yValue.equals(D.targetValue + "\r")){
            if (yValue.equals(D.targetValue)) {
                Dp[indiceDp] = dadosInt[i];
                indiceDp++;
            } else {
                Dn[indiceDn] = dadosInt[i];
                indiceDn++;
            }
        }
        System.out.println();
    }

    /**
     * Gera arquivo de dicionário .txt imprimindo valores de atributo e valor original e respectivos inteiros aos quais forma mapeados
     *
     * @param caminhoPastaSalvar - onde será salvo o arquivo com o dicionário
     * @throws IOException
     */
    public static void recordDicionario(String caminhoPastaSalvar) throws IOException {
        String nomeArquivo = caminhoPastaSalvar + "\\" + D.baseName + "Dic.txt";
        String separadorDicionario = ",";
        File file = new File(nomeArquivo);
        // creates the file
        file.createNewFile();
        // creates a FileWriter Object
        FileWriter writer = new FileWriter(file);
        // Writes the content to the file

        writer.write("@Nome: " + D.baseName + "\r\n");
        writer.write("@Info: Atributos=" + D.attributesNumber + separadorDicionario + "|D|=" + D.examplesNumber + separadorDicionario + "|Dp|=" + D.numeroExemplosPositivo + separadorDicionario + "|Dn|=" + D.numeroExemplosNegativo
                + separadorDicionario + "|I|=" + D.numeroItensUtilizados + "\r\n");
        //writer.write(); 
        writer.write("@Dicionario:Item,Atributo,Valor" + "\r\n");
        for (int i = 0; i < D.numeroItensUtilizados; i++) {
            writer.write(i + separadorDicionario + D.itemAtributoStr[i] + separadorDicionario + itemValorStr[i] + "\r\n");
        }
        writer.flush();
        writer.close();
    }

    /**
     * Imprime dicionário no console. É uma alternativa ao método recordDicionario que salva em arquivo.
     *
     * @deprecated OBS: esse método pode estar defasado!
     */
    public static void imprimirDicionario() {
        System.out.println("@Nome:" + D.baseName);
        System.out.println("@Info:Atributos=" + D.attributesNumber + " ; |D|=" + D.examplesNumber + " ; |Dp|=" + D.numeroExemplosPositivo + " ; |Dn|=" + D.numeroExemplosNegativo
                + "; |I|=" + D.numeroItensUtilizados);
        //System.out.println("@Dicionario: Item;atributoOriginal;valorOriginal;atributoInt;valorInt");
        System.out.println("@Dicionario: Item;Atributo;Valor");
        for (int i = 0; i < D.numeroItensUtilizados; i++) {
            //System.out.println(i + ";" + D.itemAtributoStr[i] + ";" + itemValorStr[i] + ";" + D.itemAtributo[i] + ";" + D.itemValor[i]);
            System.out.println(i + ";" + D.itemAtributoStr[i] + ";" + itemValorStr[i]);
        }
    }

    /**
     * Filtra atributos, valores e itens (atributo,valor) passados como parâmetros. Os itens filtrados não serão consideraodos pelos algoritmos nas buscas.
     *
     * @param atributos
     * @param valores
     * @param atributosValores
     */
    public static void filtrar(String[] atributos, String[] valores, String[][] atributosValores) {
        ArrayList<Integer> itensPosFiltro = new ArrayList<>();
        for (int i = 0; i < D.numeroItens; i++) {
            if (D.filtroAtributoContempla(atributos, i)
                    || D.filtroValorContempla(valores, i)
                    || D.filtroAtributoValorContempla(atributosValores, i)) {
            } else {
                itensPosFiltro.add(i); //Adicione caso não perteça a nenhum filtro
            }
        }

        D.numeroItensUtilizados = itensPosFiltro.size();
        D.itensUtilizados = new int[D.numeroItensUtilizados];
        for (int i = 0; i < D.itensUtilizados.length; i++) {
            D.itensUtilizados[i] = itensPosFiltro.get(i);
        }
    }

    /**
     * Método retorna se item passado como parâmetro pertence ao grupo de atributos que devem ser desconsiderados na busca
     *
     * @param atributos - String[] com valores de atributos que devem ser filtrados
     * @param item - item que deve ou não ser filtrado com base no filtro
     * @return
     */
    private static boolean filtroAtributoContempla(String[] atributos, int item) {
        if (atributos == null) {
            return false;
        } else {
            for (int j = 0; j < atributos.length; j++) {
                //if(D.comparaStrVar(atributos[j], D.itemAtributoStr[item])){
                if (atributos[j].equals(itemAtributoStr[item])) {
                    return true;
                }
            }
        }
        return false;
    }

    /**
     * Método retorna se item passado como parâmetro pertence ao grupo de VALORES que devem ser desconsiderados na busca
     *
     * @param valores - String[] com valores de atributos que devem ser filtrados
     * @param item - item que deve ou não ser filtrado com base no filtro
     * @return
     */
    private static boolean filtroValorContempla(String[] valores, int item) {
        if (valores == null) {
            return false;
        } else {
            for (int j = 0; j < valores.length; j++) {
                //if( D.comparaStrVar(valores[j], D.itemValorStr[item]) ){
                if (valores[j].equals(D.itemValorStr[item])) {
                    return true;
                }
            }
        }
        return false;
    }

    /**
     * Método retorna se item passado como parâmetro pertence ao grupo de intens (atributo, valor) que devem ser desconsiderados na busca
     *
     * @param atributosValores - String[][]
     * @param item - item que deve ou não ser filtrado com base no filtro
     * @return
     */
    private static boolean filtroAtributoValorContempla(String[][] atributosValores, int item) {
        if (atributosValores == null) {
            return false;
        } else {
            for (int j = 0; j < atributosValores.length; j++) {
                //if(D.comparaStrVar(atributosValores[j][0], D.itemAtributoStr[item]) &&
                //   D.comparaStrVar(atributosValores[j][1], D.itemValorStr[item]) ){
                if (atributosValores[j][0].equals(D.itemAtributoStr[item])
                        && atributosValores[j][1].equals(D.itemValorStr[item])) {

                    return true;
                }
            }
        }
        return false;
    }

    /**
     * Compara duas strings com variações de formatos provavelmente devido a fomatação do testo (ISO, ANSI, etc.) Não sei se estácobrindo todoas as possibilidades. Deve ter uma forma mais elegante de lidar com esse problema!!!
     *
     * @param palavra
     * @param palavraVariacoes
     * @return
     */
    private static boolean comparaStrVar(String palavraVariacoes, String palavra) {
        return (palavra.equals(palavraVariacoes) //|| 
                //palavra.equals( "\"" + palavraVariacoes  + "\"") || 
                //palavra.equals( "\"" + palavraVariacoes  + "\"\r") || 
                //palavra.equals("\'" + palavraVariacoes  + "\'\r") || 
                //palavra.equals(palavraVariacoes  + "\r")
                );
    }

    public static void main(String args[]) throws FileNotFoundException, IOException {

//        String path = Const.CAMINHO_BASES + "amazon_cells_labelled.csv";
//        
//        D.loadFile(path, D.TIPO_CSV);
//              
//        System.out.println();
        String caminhoPastaArquivos = Const.CAMINHO_BASES;

        File diretorio = new File(caminhoPastaArquivos);
        File arquivos[] = diretorio.listFiles();
        D.SEPARADOR = ",";
        for (int i = 0; i < arquivos.length; i++) {
            //for(int i = 0; i < 2; i++){  
            String caminhoBase = arquivos[i].getAbsolutePath();
            D.loadFile(caminhoBase, D.TIPO_CSV);
            D.setup("p");
            System.out.println("[" + i + "]");
            //D.imprimirDicionario();
            D.recordDicionario(Const.CAMINHO_DICIONARIOS);
        }
    }

}
