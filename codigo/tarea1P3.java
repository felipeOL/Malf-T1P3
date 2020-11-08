/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */

import java.io.FileNotFoundException;

/**
 *
 * @author felip
 */
public class tarea1P3 {
    
    public static void main(String[] args) throws FileNotFoundException{
        LecturaHelp lector = new LecturaHelp();
        lector.cantidadLineas(args[0]);
        lector.leerArchivo(args[0]);
        lector.conjuntosRecursivos();
        System.out.println(lector.toString());
    }
    
}
