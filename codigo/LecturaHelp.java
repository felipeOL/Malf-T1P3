/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */

import java.io.File;
import java.io.FileNotFoundException;
import java.rmi.server.SocketSecurityException;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.Scanner;
import java.util.TreeMap;
import java.util.TreeSet;
import java.util.HashMap;
import java.util.Map;

/**
 *
 * @author felip
 */
public class LecturaHelp {
    Scanner lector;
    int cantidadLineas = 0;
    int contador = 0;
    
    
    //------------------------------
    
    String nombreAFD;
    TreeSet<String> estados;
    String estadoInicio;
    ArrayList<String> sigmas;
    ArrayList<Transicion> listaTrancisiones;
    ArrayList<String> estadosFinales;
    ArrayList<Conjunto> conjuntos;
    ArrayList<Conjunto> nuevoConjunto;
    ArrayList<Conjunto> conjuntoConEstados;
    
    public LecturaHelp(){
        this.estados = new TreeSet<String>();
        this.sigmas = new ArrayList<String>();
        this.listaTrancisiones = new ArrayList<>();
        this.estadosFinales = new ArrayList<>();
        this.conjuntos = new ArrayList<>();
        this.nuevoConjunto = new ArrayList<>();
        this.conjuntoConEstados = new ArrayList<>();
    }
    
    public void leerArchivo(String nombreArchivo) throws FileNotFoundException{
        lector = new Scanner(new File(nombreArchivo));
        
        //Se guarda el nombre de AFD
        nombreAFD = lector.nextLine();
        //Se guardan los estados del AFD
        String[] listaEstados = lector.nextLine().split("K=\\{")[1].split("\\}")[0].split(",");
        for (String estadoSeleccionado : listaEstados) {
            estados.add(estadoSeleccionado);
        }
        
        //Se guardan los sigmas
        String[] sigma = lector.nextLine().split("Sigma=\\{")[1].split("\\}")[0].split(",");
        for (String string : sigma) {
            sigmas.add(string);
        }
        
        //Salto de linea de la frase Delta:
        lector.nextLine();
        
        //Se guardan las transiciones
        ArrayList<String[]> transicion = new ArrayList<String[]>();
        for (int i = 4; i < cantidadLineas - 2 ; i++) {
            String[] algo = lector.nextLine().split("\\(")[1].split("\\)")[0].split(",");
            //System.out.println(algo[0] + " " + algo[1] + " " + algo[2]);
            transicion.add(algo);
        }
        
        for (String[] strings : transicion) {
            Transicion transicionAux = new Transicion(strings[0],strings[1],strings[2]);
            listaTrancisiones.add(transicionAux);
        }
        
        //Se guarda el estado de inicio.
        estadoInicio = lector.nextLine().split("s=")[1];
        
        //Se guarda los estados finales.
        String[] estadosFinales = lector.nextLine().split("F=\\{")[1].split("\\}")[0].split(",");
        for (int i = 0; i < estadosFinales.length; i++) {
            this.estadosFinales.add(estadosFinales[i]);
        }
        
    }
    
    //Solamente es para contar las lineas para usarlo en la parte de la lectura de transiciones.
    public void cantidadLineas(String nombreArchivo) throws FileNotFoundException{
        try{
            lector = new Scanner(new File(nombreArchivo));
            
            while(lector.hasNextLine()){
                String linea = lector.nextLine();
                //System.out.println(linea);
                cantidadLineas++;
            }
            //System.out.println("cantidad lineas: "+cantidadLineas);
        }catch(Exception e){
            e.printStackTrace();
        }
    }

    private class Conjunto{
        private String nombre;
        public HashMap<String, ArrayList<String>> listaFilas;
        public Conjunto(int nombre) {
            this.nombre = String.valueOf(nombre);
            this.listaFilas = new HashMap<String, ArrayList<String>>();
        }
    }

    public void conjuntosRecursivos(){
        // crear matrices
        // verificar orden
        Conjunto conjuntoInicial = new Conjunto(this.contador++);
        for (String estado : estados) {
            conjuntoInicial.listaFilas.put(estado, new ArrayList<String>());
            for (Transicion transicion : listaTrancisiones) {
                for (String simbolo : sigmas) {
                    if(transicion.inicio.equals(estado) && transicion.sigma.equals(simbolo)){
                        conjuntoInicial.listaFilas.get(estado).add(transicion.llegada);
                    }
                }
            }
        }
        Conjunto conjuntoFinal = new Conjunto(this.contador++);
        for (String estadoFinal : this.estadosFinales) {
            conjuntoFinal.listaFilas.put(estadoFinal, conjuntoInicial.listaFilas.get(estadoFinal));
            conjuntoInicial.listaFilas.remove(estadoFinal);
        }
        this.nuevoConjunto = new ArrayList<Conjunto>();
        this.nuevoConjunto.add(conjuntoFinal);
        this.nuevoConjunto.add(conjuntoInicial);
        this.agruparRecursivo(false);
        this.translate(this.nuevoConjunto);
    }

    private void translate(ArrayList<Conjunto> conjuntosTemp) {
        // Reemplazamos los estados por su grupo respectivo
        for (Conjunto conjunto : conjuntosTemp) {
            for (Map.Entry<String, ArrayList<String>> fila : conjunto.listaFilas.entrySet()) {
                String estadoActual = fila.getKey();
                int i = fila.getValue().size() - 1;
                for (String estadoEnTransicion : (ArrayList<String>) fila.getValue().clone()) { // Este tira un warning
                    for (Conjunto repasadaConjunto : conjuntosTemp) {
                        if (repasadaConjunto.listaFilas.keySet().contains(estadoEnTransicion)) {
                            conjunto.listaFilas.get(estadoActual).remove(i--);
                            conjunto.listaFilas.get(estadoActual).add(repasadaConjunto.nombre);
                        }
                    }
                }
            }
        }
    }

    private ArrayList<HashMap<String, ArrayList<String>>> group(ArrayList<Conjunto> conjuntosTemp, boolean todosOk) {
        ArrayList<HashMap<String, ArrayList<String>>> nuevosConjuntos = new ArrayList<HashMap<String, ArrayList<String>>>();
        // Para cada Conjunto<Tengo Filas<Que tienen Pares que asocian una ->,Lista de <Estados>>>
        int conjuntoActual = 0;
        for (Conjunto conjunto : conjuntosTemp) {
            String anterior = "jajaasies";
            int i = 0;
            nuevosConjuntos.add(new HashMap<String, ArrayList<String>>());
            for (Map.Entry<String, ArrayList<String>> fila : conjunto.listaFilas.entrySet()) {
                if (i == 0) {
                    anterior = fila.getValue().toString();
                    if (nuevosConjuntos.get(conjuntoActual).get(anterior) == null) {
                        nuevosConjuntos.get(conjuntoActual).put(anterior, new ArrayList<String>());
                    }
                    nuevosConjuntos.get(conjuntoActual).get(anterior).add(fila.getKey());
                    i++;
                    continue;
                }
                String actual = fila.getValue().toString();
                if (!actual.equals(anterior)) {
                    todosOk = false;
                    if (nuevosConjuntos.get(conjuntoActual).get(actual) == null) {
                        nuevosConjuntos.get(conjuntoActual).put(actual, new ArrayList<String>());
                    }
                    nuevosConjuntos.get(conjuntoActual).get(actual).add(fila.getKey());
                } else {
                    nuevosConjuntos.get(conjuntoActual).get(anterior).add(fila.getKey());
                }
                anterior = actual;
                i++;
            }
            conjuntoActual++;
        }
        return nuevosConjuntos;
    }

    private void agruparRecursivo(boolean isTodosOk) {
        if (isTodosOk) {
            return;
        }
        boolean todosOk = true;
        // Hacemos una copia de la lista de conjuntos actual
        ArrayList<Conjunto> conjuntosTemp = new ArrayList<Conjunto>();
        for (Conjunto conjunto : this.nuevoConjunto) {
            Conjunto nuevoConjuntoIt = new Conjunto(this.contador++);
            for (Map.Entry<String, ArrayList<String>> fila : conjunto.listaFilas.entrySet()) {
                ArrayList<String> columnaCopias = new ArrayList<String>();
                for (String columna : fila.getValue()) {
                   columnaCopias.add(columna);
                }
                nuevoConjuntoIt.listaFilas.put(fila.getKey(), columnaCopias);
            }
            conjuntosTemp.add(nuevoConjuntoIt);
        }
        translate(conjuntosTemp);
        // Se agrupa cada estado por grupo de acuerdo a los pares que tiene
        ArrayList<HashMap<String, ArrayList<String>>> nuevosConjuntos = group(conjuntosTemp, todosOk);
        // Se regeneran los conjuntos en base a las nuevas agrupaciones
        ArrayList<Conjunto> reSeparados = new ArrayList<Conjunto>();
        int conjuntoIt = 0;
        for (HashMap<String, ArrayList<String>> nuevo : nuevosConjuntos) {
            for (Map.Entry<String, ArrayList<String>> filaNueva : nuevo.entrySet()) {
                Conjunto temp = new Conjunto(this.contador++);
                for (String estado : filaNueva.getValue()) {
                    for (Conjunto conjuntoOriginal : this.nuevoConjunto) {
                        for (Map.Entry<String, ArrayList<String>> entryOriginal : conjuntoOriginal.listaFilas.entrySet()) {
                            if (entryOriginal.getKey().equals(estado)) {
                                temp.listaFilas.put(estado, entryOriginal.getValue());
                            }
                        }
                    }
                }
                reSeparados.add(temp);
            }
        }
        ArrayList<Conjunto> respuesta = new ArrayList<Conjunto>();
        for (Conjunto conjunto : reSeparados) {
            respuesta.add(conjunto);
        }
        this.nuevoConjunto = respuesta;
        agruparRecursivo(todosOk);
    }


    // Gets the DFA in string format
    @Override
    public String toString() {
        String result = "AFD M:\nK={";
        int i = 0;
        ArrayList<HashMap<String, ArrayList<String>>> conjuntosParaImprimir = new ArrayList<HashMap<String, ArrayList<String>>>();
        // Para cada Conjunto<Tengo Filas<Que tienen Pares que asocian una ->,Lista de <Estados>>>
        int conjuntoActual = 0;
        for (Conjunto conjunto : this.nuevoConjunto) {
            String anterior = "jajaasies";
            int j = 0;
            conjuntosParaImprimir.add(new HashMap<String, ArrayList<String>>());
            for (Map.Entry<String, ArrayList<String>> fila : conjunto.listaFilas.entrySet()) {
                if (j == 0) {
                    anterior = fila.getValue().toString();
                    if (conjuntosParaImprimir.get(conjuntoActual).get(anterior) == null) {
                        conjuntosParaImprimir.get(conjuntoActual).put(anterior, new ArrayList<String>());
                    }
                    conjuntosParaImprimir.get(conjuntoActual).get(anterior).add(fila.getKey());
                    j++;
                    continue;
                }
                String actual = fila.getValue().toString();
                if (!actual.equals(anterior)) {
                    if (conjuntosParaImprimir.get(conjuntoActual).get(actual) == null) {
                        conjuntosParaImprimir.get(conjuntoActual).put(actual, new ArrayList<String>());
                    }
                    conjuntosParaImprimir.get(conjuntoActual).get(actual).add(fila.getKey());
                } else {
                    conjuntosParaImprimir.get(conjuntoActual).get(anterior).add(fila.getKey());
                }
                anterior = actual;
                j++;
            }
            conjuntoActual++;
        }
        for (HashMap<String, ArrayList<String>> conjuntoParaImprimir : conjuntosParaImprimir) {
            for (Map.Entry<String, ArrayList<String>> filaParaImprimir : conjuntoParaImprimir.entrySet()) {
                result += filaParaImprimir.getValue() + ",";
            }
        }
        result = result.substring(0, result.length() - 1);
        result += "}\nSigma={";
        i = 0;
        for (String sigma : this.sigmas) {
            result += sigma;
            result += i++ < this.sigmas.size() - 1 ? "," : "}\n";
        }
        result += "Delta:\n";
        TreeMap<String, ArrayList<String>> resultadoTransicion = new TreeMap<String, ArrayList<String>>();
        for (Conjunto conjunto : this.nuevoConjunto){
            boolean para = false;
            for (Map.Entry<String,ArrayList<String>> fila : conjunto.listaFilas.entrySet()) {
                for (Conjunto conjunto2 : this.nuevoConjunto) {
                    for (int j = 0; j < sigmas.size(); j++) {
                        if (conjunto2.nombre.equals(fila.getValue().get(j))) {
                            String origen = conjunto.listaFilas.keySet().toString();
                            String destino = conjunto2.listaFilas.keySet().toString();
                            if (resultadoTransicion.get(origen) == null) {
                                resultadoTransicion.put(origen, new ArrayList<String>());
                            }
                            resultadoTransicion.get(origen).add(destino);
                            para = true;
                            break;
                        }
                    }
                }
                if (para) {
                    break;
                }
            }
        }
        int contador = 0;
        boolean pase = false;
        int repeticion = 0;
        for (Map.Entry<String,ArrayList<String>> fila : resultadoTransicion.entrySet()) {
            while(repeticion < sigmas.size()){
                if(pase == false){
                    result += "("+fila.getKey()+","+sigmas.get(contador)+","+fila.getValue().get(contador)+")\n";
                    pase = true;
                    contador++;
                }
                else{
                    result += "("+fila.getKey()+","+sigmas.get(contador)+","+fila.getValue().get(contador)+")\n";
                    contador--;
                    pase = false;
                }
                repeticion++;
            }
            repeticion = 0;
        }

        
        result += "s=" + this.estadoInicio + "\nF={";
        i = 0;
        for (String estadoFinal : this.estadosFinales) {
            result += estadoFinal;
            result += i++ < this.estadosFinales.size() - 1 ? "," : "}\n";
        }
        return result;
    }

}
