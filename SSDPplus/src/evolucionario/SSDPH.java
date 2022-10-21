/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package evolucionario;

import dp.Avaliador;
import dp.Const;
import dp.D;
import dp.Pattern;
import exatos.GulosoD;
import java.io.FileNotFoundException;
import java.util.Arrays;
import java.util.HashSet;
import java.util.Iterator;
//import java.util.HashSet;
import java.util.Random;
import java.util.Scanner;
import simulacoes.DPinfo;

/**
 *
 * @author TARCISIO
 */
public class SSDPH {

    public static Pattern[] run(int k, String tipoAvaliacao, double similaridade, double maxTimeSegundos, int maxDimensao) throws FileNotFoundException {
        long t0 = System.currentTimeMillis(); //Initial time
        int j = 0;
        int kOriginal = k;
        k = 30;
        Pattern[][] pList = new Pattern[D.numberOfPartitions][];
        //Pattern[][] pCacheList = new Pattern[D.numberOfPartitions*k*Pattern.maxSimulares][];
        Pattern[] pBest;
        Pattern[] pBestSimilars;
        String partitionsInfo = "";
        Pattern[] Pk = new Pattern[k];
        Pattern[] P = null;
        for(int r = 1; r <= D.numberOfPartitions || r == 1; r++) {
            if (D.numberOfPartitions > 0) {
                D.switchPartition(r);
            }
           //Inicializa Pk com indivíduos vazios
            for (int i = 0; i < Pk.length; i++) {
                Pk[i] = new Pattern(new HashSet<Integer>(), tipoAvaliacao);
            }

            //System.out.println("Inicializando população...");
            //Inicializa garantindo que P maior que Pk sempre! em bases pequenas isso nem sempre ocorre
            Pattern[] Paux = INICIALIZAR.D1(tipoAvaliacao);//P recebe população inicial
            if (Paux.length < k) {
                P = new Pattern[k];
                for (int i = 0; i < k; i++) {
                    if (i < Paux.length) {
                        P[i] = Paux[i];
                    } else {
                        P[i] = Paux[Const.random.nextInt(Paux.length - 1)];
                    }
                }
            } else {
                P = Paux;
            }

            Arrays.sort(P);

            //System.arraycopy(P, 0, Pk, 0, k); //Inicializa Pk com os melhores indivíduos da população inicial
            SELECAO.salvandoRelevantesDPmais(Pk, P, similaridade);

            int numeroGeracoesSemMelhoraPk = 0;
            int indiceGeracoes = 1;

            //Genetic Algorithmn loop
            Pattern[] Pnovo = null;
            Pattern[] PAsterisco = null;

            int tamanhoPopulacao = P.length;
            int repetitionsCount;
            //System.out.println("Buscas...");
            for (int numeroReinicializacoes = 0; numeroReinicializacoes < 3; numeroReinicializacoes++) {//Controle número de reinicializações
                //System.out.println("Reinicialização: " + numeroReinicializacoes);
                if (numeroReinicializacoes > 0) {
                    P = INICIALIZAR.aleatorio1_D_Pk(tipoAvaliacao, tamanhoPopulacao, Pk);
                    //repetitionsCount = 0;
                }
                repetitionsCount = 0;
                double mutationTax = 0.4; //Mutação inicia em 0.4. Crossover é sempre 1-mutationTax.
                //System.out.println("============================");
                while (numeroGeracoesSemMelhoraPk < 3 || repetitionsCount < 30) {

                    if (indiceGeracoes == 1) {
                        Pnovo = CRUZAMENTO.ANDduasPopulacoes(P, P, tipoAvaliacao);
                        indiceGeracoes++;
                    } else {
                        Pnovo = CRUZAMENTO.uniforme2Pop(P, mutationTax, tipoAvaliacao);
                    }
                    PAsterisco = SELECAO.selecionarMelhores(P, Pnovo);
                    P = PAsterisco;

                    int novosK = SELECAO.salvandoRelevantesDPmais(Pk, PAsterisco, similaridade);//Atualizando Pk e coletando número de indivíduos substituídos
                    double tempo = (System.currentTimeMillis() - t0) / 1000.0; //time
                    if (maxTimeSegundos > 0 && tempo > maxTimeSegundos) {
                        return Pk;
                    }
                    //System.out.println("Modificações em Pk: " + novosK);
                    //Definição automática de mutação de crossover
                    if (novosK > 0 && mutationTax > 0.0) {//Aumenta cruzamento se Pk estiver evoluindo e se mutação não não for a menos possível.
                        mutationTax -= 0.2;
                    } else if (novosK == 0 && mutationTax < 1.0) {//Aumenta mutação caso Pk não tenha evoluido e mutação não seja maior que o limite máximo.
                        mutationTax += 0.2;
                    }
                    //Critério de parada: 3x sem evoluir Pk com taxa de mutação 1.0
                    if (novosK == 0 && mutationTax == 1.0) {
                        numeroGeracoesSemMelhoraPk++;

                    } else {
                        repetitionsCount = 30;
                        numeroGeracoesSemMelhoraPk = 0;
                    }    
                    repetitionsCount++;
                }

                numeroGeracoesSemMelhoraPk = 0;
            }
            //D.switchPartition(0);
            //Avaliador.evaluateWholeBase(Pk, tipoAvaliacao); 
            if (D.numberOfPartitions > 0) {
                Avaliador.evaluateWholeBase(Pk, tipoAvaliacao); 
                pList[j++] = Pk;
                //partitionsInfo += printInfo(j);
            }
        }
        //return Pbest;

        if (D.numberOfPartitions > 0) {
            pBest = pList[0];
            Pattern[] pTopK;
            for (int i = 1; i < pList.length; i++) {
                pTopK = pList[i];
                SELECAO.salvandoRelevantesDPmais(pBest, pTopK, similaridade);
                for(int c = 0; c < pTopK.length; c++ ){
                    pBestSimilars = pTopK[c].getSimilares();
                    if(pBestSimilars != null){
                        SELECAO.salvandoRelevantesDPmais(pBest, pBestSimilars, similaridade);
                    }
                }
            }
            //System.out.println(partitionsInfo);
            return pBest;
            
        } else {
            
            HashSet<Integer> itensPk = new HashSet<>();
            for(int n = 0; n < Pk.length; n++){
                itensPk.addAll(Pk[n].getItens());
                Pattern[] similaresPk = Pk[n].getSimilares();
                if(similaresPk != null){
                    for(int m = 0; m < similaresPk.length; m++){
                        itensPk.addAll(similaresPk[m].getItens());

                    }
                }
            }
            int[] itensPkArray = new int[itensPk.size()];

            Iterator iterator = itensPk.iterator();
            int n = 0;        
            while(iterator.hasNext()){
                itensPkArray[n++] = (int)iterator.next();
            }
            
            /*int maiorNumeroItens = Integer.MIN_VALUE;
            for(int c = 0; c < Pk.length; c++){
                if(Pk[c].getItens().size() > maiorNumeroItens){
                    maiorNumeroItens = Pk[c].getItens().size();
                }
            }*/
            Pattern[] PkBigger = new Pattern[kOriginal]; //new Pattern[kOriginal*Pattern.maxSimulares];
            int i = 0;
            for(int c = 0; c < Pk.length; c++){
                if(Pk[c].getItens().size() > maxDimensao){
                    PkBigger[i++] = Pk[c];
                }
                /*Pattern[] similaresPk = Pk[c].getSimilares();
                if(similaresPk != null){
                    for(int m = 0; m < similaresPk.length; m++){
                        if(similaresPk[m].getItens().size() >=maxDimensao){
                            PkBigger[i++] = similaresPk[m];
                        }

                    }
                }*/
            }
            D.itensUtilizados = itensPkArray;
            D.numeroItensUtilizados = itensPkArray.length;
            
            Pattern[] PkExhaustive = GulosoD.run(kOriginal, D.numeroItensUtilizados, tipoAvaliacao, similaridade, maxTimeSegundos, maxDimensao);
            SELECAO.salvandoRelevantesDPmais(PkExhaustive, PkBigger, similaridade);
            Pk = PkExhaustive;
            return Pk;
        }
    }

    public static void main(String args[]) throws FileNotFoundException {
        //*******************************************
        //Data set                    ***************
        //*******************************************
        String caminho = "D:\\giord\\Giordano\\CC - UNICAP\\PIBIC\\PROJETO SSDP+\\Definitive SSDPH Folder\\ssdp_plus\\data sets\\Text mining\\";
        String nomeBase = "matrixBinaria-Global-100-p.CSV";
        String caminhoBase = caminho + nomeBase;

        //separator database (CSV files)
        D.SEPARADOR = ",";
        //Seed
        Const.random = new Random(Const.SEEDS[0]);
        //*******************************************
        //END Data set                ***************
        //*******************************************

        //*******************************************
        //SSDP+ parameters            ***************
        //*******************************************
        //k: number of subgroups
        int k = 10;
        //Evaluation metric
        String tipoAvaliacao = Avaliador.METRICA_AVALIACAO_WRACC;
        //String tipoAvaliacao = Avaliador.METRICA_AVALIACAO_QG;
        //ks: cache size
        Pattern.maxSimulares = 5;
        //min_similarity
        double similaridade = 0.90;
        //Similarity function
        Pattern.medidaSimilaridade = Const.SIMILARIDADE_JACCARD; //similarity function (default JACCARD)
        //Target (atributevalue)
        String target = "p";

        //*******************************************
        //END SSDP+ parameters            ***************
        //*******************************************
        //It is not about the SSDP+
        Pattern.ITENS_OPERATOR = Const.PATTERN_AND;

        //max time simulation in second (-1 for infinity)
        double maxTimeSecond = -1;
        int maxDimensao;

        System.out.println("Loading data set...");
        D.loadFile(caminhoBase, D.TIPO_CSV); //Loading data set 

        //*******************************************
        //User Interaction             ***************
        //*******************************************
        Scanner input = new Scanner(System.in);
        int choice;
        System.out.println("\nEscolha uma das opções:");
        System.out.println("1 - Processar a base inteira");
        System.out.println("2 - Processar amostra da base");
        System.out.println("3 - Separar em partições");
        System.out.print("Escolha: ");
        choice = input.nextInt();
        if (choice != 1) {
            if (choice == 3) {
                System.out.print("Digite o número de partições: ");
                int partitionsNumber = input.nextInt();
                if (partitionsNumber == 1) {
                    choice = 2;
                } else {
                    D.createPartitions(partitionsNumber);
                }
            }
            if (choice == 2) {
                System.out.print("Digite a porcentagem da amostra: ");
                double totalSampleRate = input.nextDouble();
                System.out.print("Digite a porcentagem de exemplos positivos na amostra (ex: 40%): ");
                double samplingRate = input.nextDouble();
                //D.getSampledMatrix();
                D.createOnePartition(totalSampleRate / 100, samplingRate / 100);

            }
        }
        System.out.print("Escolha o número máximo de dimensões (recomendado 3): ");
        maxDimensao = input.nextInt();
        //*******************************************
        //End User Interaction        ***************
        //*******************************************

        D.setup(target);
        //D.getSampledMatrix(0.4);
        //"6,80,104,116,134,145,151,153,156,256"; //target value
        //D.targetValue = "I-III";
        //D.targetValue = "IV-VII";

        //*******************************************
        //FILTER BY ATTRIBUTE, VALUES AND ITEMS *****
        //*******************************************
        //Filter by attribute
        //String[] filtrarAtributos = {"x.X267"};
        String[] filtrarAtributos = null;
        //Filter by values
        String[] filtrarValores = null;
        //String[] filtrarValores = {"", "NA"};
        //Filter by items
        String[][] filtrarAtributosValores = null;
        //String[][] filtrarAtributosValores = new String[2][2];
        //filtrarAtributosValores[0][0] = "D001";
        //filtrarAtributosValores[0][1] = "2";
        //filtrarAtributosValores[1][0] = "E01002";
        //filtrarAtributosValores[1][1] = "8";
        //*******************************************
        //EDN FILTER BY ATTRIBUTE, VALUES AND ITEMS *****
        //*******************************************

        //Executar filtros        
        D.filtrar(filtrarAtributos, filtrarValores, filtrarAtributosValores);

        Pattern.numeroIndividuosGerados = 0; //Initializing count of generated individuals

        Pattern[] pk;
       

        System.out.println("SSDP+ running...");
        //Rodando SSDP
        long t0 = System.currentTimeMillis(); //Initial time
        //Pattern[] p = SSDPH.run(k, tipoAvaliacao, similaridade);
        pk = SSDPH.run(k, tipoAvaliacao, similaridade, maxTimeSecond, 1);
        
        double tempo = (System.currentTimeMillis() - t0) / 1000.0; //time
        
        System.out.println("\n### Top-k subgroups:");
        Avaliador.imprimirRegras(pk, k);

        //Informations about top-k DPs:  
        System.out.println("\n### Data set:" + D.baseName + "(|I|=" + D.numeroItens
                + "; |A|=" + D.attributesNumber
                + "; |D+|=" + D.numeroExemplosPositivo
                + "; |D-|=" + D.numeroExemplosNegativo
                + ")"
        ); //database name
        System.out.println("Average " + tipoAvaliacao + ": " + Avaliador.avaliarMedia(pk, k));
        System.out.println("Time(s): " + tempo);
        System.out.println("Average size: " + Avaliador.avaliarMediaDimensoes(pk, k));
        System.out.println("Coverage of all Pk DPs in relation to D+: " + Avaliador.coberturaPositivo(pk, k) * 100 + "%");
        System.out.println("Description Redundancy Item Dominador (|itemDominador|/k): " + DPinfo.descritionRedundancyDominator(pk));
        System.out.println("Number of individuals generated: " + Pattern.numeroIndividuosGerados);

        System.out.println("\n### Top-k and caches");
        //Avaliador.imprimirRegrasSimilares(p, k); 
        String[] metricas = {
            Const.METRICA_QUALIDADE,
            Const.METRICA_SIZE, //Const.METRICA_WRACC,
        //Const.METRICA_Qg,
        //Const.METRICA_DIFF_SUP,
        //Const.METRICA_LIFT,
        //Const.METRICA_CHI_QUAD,
        //Const.METRICA_P_VALUE,
        //Const.METRICA_SUPP_POSITIVO,
        //Const.METRICA_SUPP_NEGATIVO,
        //Const.METRICA_COV,
        //Const.METRICA_CONF            
        };
        Avaliador.imprimirRegras(pk, k, metricas, false, false, true);

    }

    public static String printInfo(int j) {
        return "[Partition " + (j) + "]";
    }

}
