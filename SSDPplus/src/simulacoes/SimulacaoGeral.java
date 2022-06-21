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
    private int getIndiceUltimaSimulacao() throws FileNotFoundException, IOException{
        Scanner sc = new Scanner(this.fileIndiceUltimaSimulacao);
        String indicesStr = sc.nextLine();
        return Integer.parseInt( indicesStr );
    }
    
    //Atualizar os índices inciais no arquivo
    private void atualizarIndiceUltimaSimulacao(int indiceUltimaSimulacao) throws IOException{
        FileWriter fileWriter = new FileWriter(this.fileIndiceUltimaSimulacao);
        BufferedWriter bufferedWriter = new BufferedWriter(fileWriter);
        bufferedWriter.write( indiceUltimaSimulacao + "" );
        bufferedWriter.close();
    }
    
    //Salvar resultados das simulações em arquivo
    private void salvarResultado(Simulacao s) throws IOException{
        String nomeAlgoritmo = s.getAlgoritmo();
        String nomeBase = s.getNomeBase();
        
        StringBuilder sb = new StringBuilder();
        Resultado[] resultados = s.getResultados();        
        for(int i = 0; i < resultados.length; i++){
            Resultado r = resultados[i];
            sb.append( "@rep:" + (i+1) );
            sb.append("\n");
            sb.append( "@time:" + r.getTempoExecucao() );
            sb.append("\n");
            sb.append( "@trys:" + r.getNumeroTestes());
            sb.append("\n");
            sb.append( "@seed:" + r.getSeed());
            sb.append("\n");
            
            Pattern[] dps = r.getDPs();
            sb.append( "@dps-begin:");
            sb.append("\n");
            for(int j = 0; j < dps.length; j++){
                HashSet<Integer> itens = dps[j].getItens();
                Iterator iterator = itens.iterator();
                while(iterator.hasNext()){
                    sb.append(iterator.next() + ",");
                }
                sb.setCharAt(sb.length()-1, '\n');
                //sb.append("\n");
            }
            sb.append( "@dps-end:");
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
    public void imprimirTopkDP1(String caminhoPastaArquivos, int k) throws FileNotFoundException, IOException{
        String tipoAvaliacao = Avaliador.METRICA_AVALIACAO_QG;
                
        File diretorio = new File(caminhoPastaArquivos);
        File arquivos[] = diretorio.listFiles();
        
        //Cada Base
        for(int j = 0; j < arquivos.length; j++){                
            String caminhoBase = arquivos[j].getAbsolutePath();
            String nomeBase = arquivos[j].getName().replace(".CSV", "");
            D.loadFile(caminhoBase, D.TIPO_CSV);

            //Levantado ranking DP1 para cálculo do número de incites e incites parciais!!!
            Pattern[] DP1 = INICIALIZAR.D1(tipoAvaliacao);
            Arrays.sort(DP1);
            System.out.println("\nBase: " + arquivos[j].getName());
            for(int i = 0; i < k; i++){
                Pattern p = DP1[i];
                System.out.print("[" + i + "]:" +  p.getQualidade() + ",");
            }
        }
        
        
    }
    
    public void run(int[] K, int numeroRepeticoes, String[] algoritmos, String separadorBase, String tipoAvaliacao, double tempoMaximoSegundosAlgoritmos) throws FileNotFoundException, IOException{        
        
        D.SEPARADOR = separadorBase;
        File diretorio = new File(Const.CAMINHO_BASES);
        File arquivos[] = diretorio.listFiles();
        
        int totalSimulacoes = algoritmos.length * K.length * arquivos.length;
        int indiceSimulacoes = 1; //Contrtole para simulação continuar de onde parou caso algum erro ocorra.
        for(int i = 0; i < K.length; i++){
            int k = K[i];           
            Pattern.maxSimulares = k;
            //Cada Base
            for(int j = 0; j < arquivos.length; j++){                
                String caminhoBase = arquivos[j].getAbsolutePath();
                String nomeBase = arquivos[j].getName().replace(".CSV", "");
                nomeBase = nomeBase.replace(".csv", "");
                
                D.loadFile(caminhoBase, D.TIPO_CSV);
                D.setup("p");
                
                //Cada algoritmo
                for(int m = 0; m < algoritmos.length; m++){                    
                    
                    if(indiceSimulacoes < this.indiceUltimaSimulacao){                        
                        indiceSimulacoes++;
                        continue;
                    }else{
                        //this.atualizarIndiceUltimaSimulacao(indiceSimulacoes);//Salvando índices inciais
                    }
                    
                    String algoritmo = algoritmos[m];
                    Resultado[] resultados = new Resultado[numeroRepeticoes];
                    System.out.println("\n\n[" + indiceSimulacoes + "/" + totalSimulacoes + "]: K[" + i + "]:" + k + " Base[" + j + "]:" + nomeBase + " - Alg[" + m + "]:" + algoritmo);
                    //Cada repetição
                    System.out.print("Repeticao:");
                    for(int n = 0; n < numeroRepeticoes; n++){                    
                        System.out.print(n+",");
                        Pattern.numeroIndividuosGerados = 0;
                        Pattern[] p = null;
                        Const.random = new Random(Const.SEEDS[n]);
                        long t0 = System.currentTimeMillis();                
                        switch(algoritmo){
                            case Const.ALGORITMO_SSDPmais:
                                p = SSDPmais.run(k, tipoAvaliacao, 0.7, tempoMaximoSegundosAlgoritmos);
                                break;
                            case Const.ALGORITMO_SSDPmaisS00:
                                p = SSDPmais.run(k, tipoAvaliacao, 0.0, tempoMaximoSegundosAlgoritmos);
                                break;
                            case Const.ALGORITMO_SSDPmaisS10:
                                p = SSDPmais.run(k, tipoAvaliacao, 0.1, tempoMaximoSegundosAlgoritmos);
                                break;
                            case Const.ALGORITMO_SSDPmaisS20:
                                p = SSDPmais.run(k, tipoAvaliacao, 0.2, tempoMaximoSegundosAlgoritmos);
                                break;
                            case Const.ALGORITMO_SSDPmaisS30:
                                p = SSDPmais.run(k, tipoAvaliacao, 0.3, tempoMaximoSegundosAlgoritmos);
                                break;
                            case Const.ALGORITMO_SSDPmaisS40:
                                p = SSDPmais.run(k, tipoAvaliacao, 0.4, tempoMaximoSegundosAlgoritmos);
                                break;
                            case Const.ALGORITMO_SSDPmaisS50:
                                p = SSDPmais.run(k, tipoAvaliacao, 0.5, tempoMaximoSegundosAlgoritmos);
                                break;
                            case Const.ALGORITMO_SSDPmaisS60:
                                p = SSDPmais.run(k, tipoAvaliacao, 0.6, tempoMaximoSegundosAlgoritmos);
                                break;                            
                            case Const.ALGORITMO_SSDPmaisS70:
                                p = SSDPmais.run(k, tipoAvaliacao, 0.7, tempoMaximoSegundosAlgoritmos);
                                break;                            
                            case Const.ALGORITMO_SSDPmaisS80:
                                p = SSDPmais.run(k, tipoAvaliacao, 0.8, tempoMaximoSegundosAlgoritmos);
                                break;                     
                            case Const.ALGORITMO_SSDPmaisS90:
                                p = SSDPmais.run(k, tipoAvaliacao, 0.9, tempoMaximoSegundosAlgoritmos);
                                break;
                            case Const.ALGORITMO_SSDPmaisS100:
                                p = SSDPmais.run(k, tipoAvaliacao, 1.0, tempoMaximoSegundosAlgoritmos);
                                break;
                            case Const.ALGORITMO_GulosoDplus:
                                p = GulosoD.run(k, D.numeroExemplosPositivo, tipoAvaliacao, 0.7, tempoMaximoSegundosAlgoritmos, 4);
                                break;  
                             case Const.ALGORITMO_SSDPHS90:
                                p = SSDPH.run(k, tipoAvaliacao, 0.9, tempoMaximoSegundosAlgoritmos,2);
                                break;
                            case Const.ALGORITMO_SSDPHS50:
                                p = SSDPH.run(k, tipoAvaliacao, 0.5, tempoMaximoSegundosAlgoritmos,2);
                                break;
                            case Const.ALGORITMO_SSDPHS10:
                                p = SSDPH.run(k, tipoAvaliacao, 0.1, tempoMaximoSegundosAlgoritmos,2);
                                break;
//                            case Const.ALGORITMO_SSDPmais10Pbest:
//                                p = SSDPmaisPbest.run(k, tipoAvaliacao, 0.1);
//                                break;
//                            case Const.ALGORITMO_SSDPmais50Pbest:
//                                p = SSDPmaisPbest.run(k, tipoAvaliacao, 0.5);
//                                break;
//                            case Const.ALGORITMO_SSDPmais90Pbest:
//                                p = SSDPmaisPbest.run(k, tipoAvaliacao, 0.9);
//                                break;
//                            case Const.ALGORITMO_SSDPmais10Prb:
//                                p = SSDPmaisRandom50Best50.run(k, tipoAvaliacao, 0.1);
//                                break;
//                            case Const.ALGORITMO_SSDPmais50Prb:
//                                p = SSDPmaisRandom50Best50.run(k, tipoAvaliacao, 0.5);
//                                break;
//                            case Const.ALGORITMO_SSDPmais90Prb:
//                                p = SSDPmaisRandom50Best50.run(k, tipoAvaliacao, 0.9);
//                                break;
                                
//                            case Const.ALGORITMO_SSDPmaisPbest:
//                                p = SSDPmaisPbest.run(k, tipoAvaliacao, 0.8);
//                                break;
//                            case Const.ALGORITMO_SSDPmaisPbest2k:
//                                p = SSDPmaisPbest2k.run(k, Math.max(2*k, 10), tipoAvaliacao, 0.8);
//                                break;
//                            case Const.ALGORITMO_SSDPmaisPbest2kselection:
//                                p = SSDPmaisPbest2kselection.run(k, Math.max(2*k, 10), tipoAvaliacao, 0.8);
//                                break;
//                            case Const.ALGORITMO_SSDPmaisRandom50Best50:
//                                p = SSDPmaisRandom50Best50.run(k, tipoAvaliacao, 0.8);
//                                break;
//                            case Const.ALGORITMO_SSDPmaisRandom50Best502k:
//                                p = SSDPmaisRandom50Best502k.run(k, Math.max(2*k, 10), tipoAvaliacao, 0.8);
//                                break;
//                            case Const.ALGORITMO_SSDPmaisRandom50Best502kselection:
//                                p = SSDPmaisRandom50Best502kselection.run(k, Math.max(2*k, 10), tipoAvaliacao, 0.8);
//                                break;
//                            case Const.ALGORITMO_SSDPmais70NA:
//                                D.filtrar(null, new String[]{"NA"}, null);
//                                p = SSDPmais.run(k, tipoAvaliacao, 0.7);
//                                break;
//                            case Const.ALGORITMO_SSDPmais10NA:
//                                D.filtrar(null, new String[]{"NA"}, null);
//                                p = SSDPmais.run(k, tipoAvaliacao, 0.1);
//                                break;
//                            case Const.ALGORITMO_SSDPmaisPbest:
//                                p = SSDPmaisPbest.run(k, tipoAvaliacao, 0.7);
//                                break;
//                            case Const.ALGORITMO_SSDPmaisPnew:
//                                p = SSDPmaisPnew.run(k, tipoAvaliacao, 0.7);
//                                break;    
//                            case Const.ALGORITMO_SSDPmaisRandom:
//                                p = SSDPmaisRandom.run(k, tipoAvaliacao, 0.7);
//                                break;
//                            
                        }                       
                                                
                        double tempo = (System.currentTimeMillis() - t0)/1000.0;
                        int numeroTentativas = Pattern.numeroIndividuosGerados;
                        
                        if(n == numeroRepeticoes-1){
                            System.out.println("\nÚltima repetição:");
                            System.out.println("Qualidade média: " + Avaliador.avaliarMedia(p,k));
                            System.out.println("Dimensão média: " + Avaliador.avaliarMediaDimensoes(p,k));        
                            System.out.println("Cobertura +: " + Avaliador.coberturaPositivo(p,k));
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
                    indiceSimulacoes++;
                }  
            }            
        }
    }
    

    
    public static void main(String args[]) throws IOException, FileNotFoundException{
                
        Pattern.ITENS_OPERATOR = Const.PATTERN_AND;
        Pattern.maxSimulares = 3;
        Pattern.medidaSimilaridade = Const.SIMILARIDADE_JACCARD;
        
        //String caminhoPastaArquivos = args[0];
        //int[] K = {5,10,20,50};
        int[] K = {10};
        //int[] K = {1,5,10,20};
        //int[] K = {5,10};
        //int numeroRepeticoes = 1;
        int numeroRepeticoes = 30; // era 30
        double  tempoMaximoSegundosAlgoritmos = -1;    // 60*60*1; //max 1h
        String[] algoritmos = {//Const.ALGORITMO_AG,
            //Const.ALGORITMO_SD,                
            
           
            Const.ALGORITMO_SSDPHS50,
         
   
            Const.ALGORITMO_SSDPmaisS50,
           
            //Const.ALGORITMO_SD,
            //Const.ALGORITMO_SD_RSS
                
            //Const.ALGORITMO_SSDPmais,
            //Const.ALGORITMO_GulosoDplus,
                            //Const.ALGORITMO_SSDPmais10Pbest,
                            //Const.ALGORITMO_SSDPmais10Prb,
//                            Const.ALGORITMO_SSDPmais50Pbest,
//                            Const.ALGORITMO_SSDPmais50Prb,
//                            Const.ALGORITMO_SSDPmais90Pbest,
//                            Const.ALGORITMO_SSDPmais90Prb,
                              
                            };
           
        
        SimulacaoGeral sg = new SimulacaoGeral(new File(Const.CAMINHO_INDICE));
        //String tipoAvaliacao = Avaliador.METRICA_AVALIACAO_QG;
        String tipoAvaliacao = Avaliador.METRICA_AVALIACAO_WRACC_NORMALIZED;
        
        sg.run(K, numeroRepeticoes, algoritmos, ",", tipoAvaliacao, tempoMaximoSegundosAlgoritmos);       
    }
}
