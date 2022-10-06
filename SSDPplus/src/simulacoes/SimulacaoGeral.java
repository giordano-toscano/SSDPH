/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package simulacoes;

import dp.Avaliador;
import dp.Const;
import dp.D;
import dp.Pattern;
//import dp.RSS;
import evolucionario.INICIALIZAR;
import evolucionario.SSDPH;
import evolucionario.SSDPmais;
//import evolucionario.*;
import exatos.GulosoD;
import java.io.BufferedWriter;
import java.io.File;
import java.io.FileNotFoundException;
import java.io.FileWriter;
import java.io.IOException;
import java.util.Arrays;
import java.util.HashSet;
import java.util.Iterator;
import java.util.Random;
import java.util.Scanner;
//import sd.SD;

/**
 *
 * @author tarcisio_pontes
 */
public class SimulacaoGeral {

    //Indices inicias de cada laço da simulação. Utilizado para a simulação continuar de onde parou caso algum erro ocorra.
    private int indiceUltimaSimulacao;
    private File fileIndiceUltimaSimulacao;

    public SimulacaoGeral(File fileIndiceUltimaSimulacao) throws FileNotFoundException, IOException {
        this.fileIndiceUltimaSimulacao = fileIndiceUltimaSimulacao;
        this.indiceUltimaSimulacao = this.getIndiceUltimaSimulacao();
    }

    //Retorna índice da última simulação realizada
    private int getIndiceUltimaSimulacao() throws FileNotFoundException, IOException {
        Scanner sc = new Scanner(this.fileIndiceUltimaSimulacao);
        String indicesStr = sc.nextLine();
        return Integer.parseInt(indicesStr);
    }

    //Atualizar os índices inciais no arquivo
    private void atualizarIndiceUltimaSimulacao(int indiceUltimaSimulacao) throws IOException {
        FileWriter fileWriter = new FileWriter(this.fileIndiceUltimaSimulacao);
        BufferedWriter bufferedWriter = new BufferedWriter(fileWriter);
        bufferedWriter.write(indiceUltimaSimulacao + "");
        bufferedWriter.close();
    }

    //Salvar resultados das simulações em arquivo
    private void salvarResultado(Simulacao s) throws IOException {
        String nomeAlgoritmo = s.getAlgoritmo();
        String nomeBase = s.getNomeBase();

        StringBuilder sb = new StringBuilder();
        Resultado[] resultados = s.getResultados();
        for (int i = 0; i < resultados.length; i++) {
            Resultado r = resultados[i];
            sb.append("@rep:" + (i + 1));
            sb.append("\n");
            sb.append("@time:" + r.getTempoExecucao());
            sb.append("\n");
            sb.append("@trys:" + r.getNumeroTestes());
            sb.append("\n");
            sb.append("@seed:" + r.getSeed());
            sb.append("\n");

            Pattern[] dps = r.getDPs();
            sb.append("@dps-begin:");
            sb.append("\n");
            for (int j = 0; j < dps.length; j++) {
                HashSet<Integer> itens = dps[j].getItens();
                Iterator iterator = itens.iterator();
                while (iterator.hasNext()) {
                    sb.append(iterator.next() + ",");
                }
                sb.setCharAt(sb.length() - 1, '\n');
                //sb.append("\n");
            }
            sb.append("@dps-end:");
            sb.append("\n");
        }
        String nomeArquivo = nomeAlgoritmo + "_" + nomeBase + ".txt";

        //Abrindo arquivo para gravação de tabelão
        File file = new File(Const.CAMINHO_RESULTADOS + nomeArquivo);
        file.createNewFile();
        // creates a FileWriter Object
        FileWriter writer = new FileWriter(file);
        writer.write(sb + "");
        writer.flush();
        writer.close();
    }

    //Imprimir DP1: 1-100
    public void imprimirTopkDP1(String caminhoPastaArquivos, int k) throws FileNotFoundException, IOException {
        String tipoAvaliacao = Avaliador.METRICA_AVALIACAO_QG;

        File diretorio = new File(caminhoPastaArquivos);
        File arquivos[] = diretorio.listFiles();

        //Cada Base
        for (int j = 0; j < arquivos.length; j++) {
            String caminhoBase = arquivos[j].getAbsolutePath();
            String nomeBase = arquivos[j].getName().replace(".CSV", "");
            D.loadFile(caminhoBase, D.TIPO_CSV);

            //Levantado ranking DP1 para cálculo do número de incites e incites parciais!!!
            Pattern[] DP1 = INICIALIZAR.D1(tipoAvaliacao);
            Arrays.sort(DP1);
            System.out.println("\nBase: " + arquivos[j].getName());
            for (int i = 0; i < k; i++) {
                Pattern p = DP1[i];
                System.out.print("[" + i + "]:" + p.getQualidade() + ",");
            }
        }

    }

    public void run(int[] K, int numeroRepeticoes, String[] algoritmos, String separadorBase, String tipoAvaliacao, double tempoMaximoSegundosAlgoritmos) throws FileNotFoundException, IOException {

        D.SEPARADOR = separadorBase;
        File diretorio = new File(Const.CAMINHO_BASES);
        File arquivos[] = diretorio.listFiles();

        int totalSimulacoes = algoritmos.length * K.length * arquivos.length;
        int indiceSimulacoes = 1; //Contrtole para simulação continuar de onde parou caso algum erro ocorra.
        for (int i = 0; i < K.length; i++) {
            int k = K[i];
            Pattern.maxSimulares = k;
            //Cada Base
            for (int j = 0; j < arquivos.length; j++) {
                String caminhoBase = arquivos[j].getAbsolutePath();
                String nomeBase = arquivos[j].getName().replace(".CSV", "");
                nomeBase = nomeBase.replace(".csv", "");

                D.loadFile(caminhoBase, D.TIPO_CSV);
                D.setup("p");

                //Cada algoritmo
                for (int m = 0; m < algoritmos.length; m++) {

                    if (indiceSimulacoes < this.indiceUltimaSimulacao) {
                        indiceSimulacoes++;
                        continue;
                    } else {
                        //this.atualizarIndiceUltimaSimulacao(indiceSimulacoes);//Salvando índices inciais
                    }

                    String algoritmo = algoritmos[m];
                    Const.random = new Random(Const.SEEDS[m]);
                    switch (algoritmo) {
                        case Const.ALGORITMO_SSDPHp20aD1:
                            D.createOnePartition(0.2, -1);
                            break;
                        case Const.ALGORITMO_SSDPHp60aD1:
                            D.createOnePartition(0.6, -1);
                            break;
                        case Const.ALGORITMO_SSDPHp20b50D1:
                            D.createOnePartition(0.2, 0.5);
                            break;
                        case Const.ALGORITMO_SSDPHp60b50D1:
                            D.createOnePartition(0.6, 0.5);
                            break;
                        case Const.ALGORITMO_SSDPHpn5D1:
                            D.createPartitions(5);
                            break;
                        case Const.ALGORITMO_SSDPHpn10D1:
                            D.createPartitions(10);
                            break;
                        default:
                            if(D.partitions != null){
                                D.switchPartition(0);
                                D.partitions = null;
                                D.numberOfPartitions = 0;
                            }
                    }
                    Resultado[] resultados = new Resultado[numeroRepeticoes];
                    System.out.println("\n\n[" + indiceSimulacoes + "/" + totalSimulacoes + "]: K[" + i + "]:" + k + " Base[" + j + "]:" + nomeBase + " - Alg[" + m + "]:" + algoritmo);
                    //Cada repetição
                    System.out.print("Repeticao:");
                    for (int n = 0; n < numeroRepeticoes; n++) {
                        System.out.print(n + ",");
                        Pattern.numeroIndividuosGerados = 0;
                        Pattern[] p = null;
                        Const.random = new Random(Const.SEEDS[n]);
                        long t0 = System.currentTimeMillis();
                        switch (algoritmo) {
                            case Const.ALGORITMO_SSDPmais:
                                p = SSDPmais.run(k, tipoAvaliacao, 0.5, tempoMaximoSegundosAlgoritmos);
                                break;
                            case Const.ALGORITMO_SSDPHD1:
                                p = SSDPH.run(k, tipoAvaliacao, 0.5, tempoMaximoSegundosAlgoritmos, 1);
                                break;
                            case Const.ALGORITMO_SSDPHD2:
                                p = SSDPH.run(k, tipoAvaliacao, 0.5, tempoMaximoSegundosAlgoritmos, 2);
                                break;
                            case Const.ALGORITMO_SSDPHD3:
                                p = SSDPH.run(k, tipoAvaliacao, 0.5, tempoMaximoSegundosAlgoritmos, 3);
                                break;
                            case Const.ALGORITMO_ED1:
                                p = GulosoD.run(k, D.numeroExemplosPositivo, tipoAvaliacao, 0.5, tempoMaximoSegundosAlgoritmos, 1);
                                break;
                            case Const.ALGORITMO_ED2:
                                p = GulosoD.run(k, D.numeroExemplosPositivo, tipoAvaliacao, 0.5, tempoMaximoSegundosAlgoritmos, 2);
                                break;
                            case Const.ALGORITMO_ED3:
                                p = GulosoD.run(k, D.numeroExemplosPositivo, tipoAvaliacao, 0.5, tempoMaximoSegundosAlgoritmos, 3);
                                break;
                            case Const.ALGORITMO_SSDPHp20aD1:
                                p = SSDPH.run(k, tipoAvaliacao, 0.5, tempoMaximoSegundosAlgoritmos, 1);
                                break;
                            case Const.ALGORITMO_SSDPHp60aD1:
                                p = SSDPH.run(k, tipoAvaliacao, 0.5, tempoMaximoSegundosAlgoritmos, 1);
                                break;
                            case Const.ALGORITMO_SSDPHp20b50D1:
                                p = SSDPH.run(k, tipoAvaliacao, 0.5,tempoMaximoSegundosAlgoritmos, 1);
                                break;
                            case Const.ALGORITMO_SSDPHp60b50D1:
                                p = SSDPH.run(k, tipoAvaliacao, 0.5, tempoMaximoSegundosAlgoritmos, 1);
                                break;
                            case Const.ALGORITMO_SSDPHpn5D1:
                                p = SSDPH.run(k, tipoAvaliacao, 0.5,tempoMaximoSegundosAlgoritmos, 1);
                                break;
                            case Const.ALGORITMO_SSDPHpn10D1:
                                p = SSDPH.run(k, tipoAvaliacao, 0.5, tempoMaximoSegundosAlgoritmos, 1);
                                break;
                        }

                        double tempo = (System.currentTimeMillis() - t0) / 1000.0;
                        int numeroTentativas = Pattern.numeroIndividuosGerados;

                        if (n == numeroRepeticoes - 1) {
                            System.out.println("\nÚltima repetição:");
                            System.out.println("Qualidade média: " + Avaliador.avaliarMedia(p, k));
                            System.out.println("Dimensão média: " + Avaliador.avaliarMediaDimensoes(p, k));
                            System.out.println("Cobertura +: " + Avaliador.coberturaPositivo(p, k));
                            System.out.println("Tempo +: " + tempo);
                            System.out.println("Tentativas +: " + numeroTentativas);
                            System.out.println("Size: " + p.length);
                            //Avaliador.imprimir(p, k);
                        }
                        resultados[n] = new Resultado(p, tempo, numeroTentativas, Const.SEEDS[n]);
                    }

                    Simulacao simulacao = new Simulacao(algoritmo + "-k" + K[i] + "-fo" + tipoAvaliacao, nomeBase, resultados);
                    //String nome = "s" + indiceSimulacoes + "-" + k + "-" + nomeBase + "-" + algoritmo + ".ser"; 

                    // write object to file
//                    FileOutputStream fos = new FileOutputStream(Const.CAMINHO_RESULTADOS_OBJ + nome);
//                    ObjectOutputStream oos = new ObjectOutputStream(fos);
//                    oos.writeObject(simulacao);
//                    oos.close();                 
                    this.salvarResultado(simulacao);
                    if(D.partitions != null){
                                D.switchPartition(0);
                                D.partitions = null;
                                D.numberOfPartitions = 0;
                            }
                    indiceSimulacoes++;
                }
            }
        }
    }

    public static void main(String args[]) throws IOException, FileNotFoundException {

        Pattern.ITENS_OPERATOR = Const.PATTERN_AND;
        Pattern.maxSimulares = 3;
        Pattern.medidaSimilaridade = Const.SIMILARIDADE_JACCARD;

        //String caminhoPastaArquivos = args[0];
        //int[] K = {5,10,20,50};
        int[] K = {10};
        //int[] K = {1,5,10,20};
        //int[] K = {5,10};
        //int numeroRepeticoes = 1;
        int numeroRepeticoes = 5; // era 30
        double tempoMaximoSegundosAlgoritmos = -1;    // 60*60*1; //max 1h
        String[] algoritmos = {//Const.ALGORITMO_AG,

            //Const.ALGORITMO_ED1,
            //Const.ALGORITMO_SSDPmais,
            //Const.ALGORITMO_SSDPHD1,
            /*Const.ALGORITMO_SSDPHp20aD1,
            Const.ALGORITMO_SSDPHp60aD1,
            Const.ALGORITMO_SSDPHp20b50D1,
            Const.ALGORITMO_SSDPHp60b50D1,
            Const.ALGORITMO_SSDPHpn5D1,*/
            //Const.ALGORITMO_SSDPHpn10D1,
            //Const.ALGORITMO_ED2,
            Const.ALGORITMO_SSDPHD2,
            /*Const.ALGORITMO_ED3,
            Const.ALGORITMO_SSDPHD3,*/
        };

        SimulacaoGeral sg = new SimulacaoGeral(new File(Const.CAMINHO_INDICE));
        String tipoAvaliacao = Avaliador.METRICA_AVALIACAO_WRACC_NORMALIZED;
        //String tipoAvaliacao = Avaliador.METRICA_AVALIACAO_QG;

        sg.run(K, numeroRepeticoes, algoritmos, ",", tipoAvaliacao, tempoMaximoSegundosAlgoritmos);
    }
}
