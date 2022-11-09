/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package dp;

import java.io.File;
import java.io.FilenameFilter;

/**
 *
 * @author giord
 */
public class Filters implements FilenameFilter {

    @Override
    public boolean accept(File dir, String name) {
        return name.toLowerCase().endsWith(".csv") || name.toLowerCase().endsWith(".txt");
    }
}
