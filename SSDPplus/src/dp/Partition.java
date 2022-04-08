/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package dp;

/**
 *
 * @author giord
 */
public class Partition {
    
    private int examplesNumber;
    private int numeroExemplosPositivo;
    private int numeroExemplosNegativo;
    
    private int[][] Dp;
    private int[][] Dn;
    
    private String[][] examplesMatrix;

    public void setExamplesNumber(int examplesNumber) {
        this.examplesNumber = examplesNumber;
    }

    public void setNumeroExemplosPositivo(int numeroExemplosPositivo) {
        this.numeroExemplosPositivo = numeroExemplosPositivo;
    }

    public void setNumeroExemplosNegativo(int numeroExemplosNegativo) {
        this.numeroExemplosNegativo = numeroExemplosNegativo;
    }

    public void setDp(int[][] Dp) {
        this.Dp = Dp;
    }

    public void setDn(int[][] Dn) {
        this.Dn = Dn;
    }

    public void setExamplesMatrix(String[][] examplesMatrix) {
        this.examplesMatrix = examplesMatrix;
    }
    
    
}
