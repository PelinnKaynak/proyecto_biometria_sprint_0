// ---------------------------------------------------------------------------
// Fichero:     PeticionarioREST.java
// Descripción: Realiza una petición HTTP (REST) en segundo plano y entrega
//              el código y el cuerpo de la respuesta mediante un callback.
//              Diseño en doc/android_design.md.
// Autor:       Jordi Bataller i Mascarell (código original proporcionado)
// Aportación:  Pelin Kaynak - cabeceras y diseño lógico (29/09/2026)
// Copyright:   (c) Jordi Bataller i Mascarell - EPSG-UPV
// ---------------------------------------------------------------------------

package com.pelinkaynak.biometria;

import java.io.BufferedReader;
import java.io.DataOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.net.HttpURLConnection;
import java.net.URL;
import android.os.AsyncTask;
import android.util.Log;

// ------------------------------------------------------------------------
// ------------------------------------------------------------------------
public class PeticionarioREST extends AsyncTask<Void, Void, Boolean> {

    // --------------------------------------------------------------------
    // --------------------------------------------------------------------
    public interface RespuestaREST {
        void callback (int codigo, String cuerpo);
    }

    // --------------------------------------------------------------------
    // --------------------------------------------------------------------
    private String elMetodo;
    private String urlDestino;
    private String elCuerpo = null;
    private RespuestaREST laRespuesta;

    private int codigoRespuesta;
    private String cuerpoRespuesta = "";

    // --------------------------------------------------------------------
    // metodo: Text, urlDestino: Text, cuerpo: Text --> hacerPeticionREST() -->
    // (la respuesta ( codigo: Z, cuerpo: Text ) llega por el callback,
    //  que se omite en la notación)
    // --------------------------------------------------------------------
    public void hacerPeticionREST (String metodo, String urlDestino, String cuerpo, RespuestaREST  laRespuesta) {
        this.elMetodo = metodo;
        this.urlDestino = urlDestino;
        this.elCuerpo = cuerpo;
        this.laRespuesta = laRespuesta;

        this.execute(); // otro thread ejecutará doInBackground()
    }

    // --------------------------------------------------------------------
    // PeticionarioREST() -->
    // --------------------------------------------------------------------
    public PeticionarioREST() {
        Log.d("clienterestandroid", "constructor()");
    }

    // --------------------------------------------------------------------
    // doInBackground() --> T/F          (privado: lo llama AsyncTask en otro hilo)
    // --------------------------------------------------------------------
    @Override
    protected Boolean doInBackground(Void... params) {
        Log.d("clienterestandroid", "doInBackground()");

        try {

            // envio la peticion

            // pagina web para hacer pruebas: URL url = new URL("https://httpbin.org/html");
            // ordinador del despatx 158.42.144.126 // OK URL url = new URL("http://158.42.144.126:8080");

            Log.d("clienterestandroid", "doInBackground() me conecto a >" + urlDestino + "<");

            URL url = new URL(urlDestino);

            HttpURLConnection connection = (HttpURLConnection) url.openConnection();
			connection.setRequestProperty( "Content-Type", "application/json; charset-utf-8" );
            connection.setRequestMethod(this.elMetodo);
            // connection.setRequestProperty("Accept", "*/*);

            // connection.setUseCaches(false);
            connection.setDoInput(true);

            if ( ! this.elMetodo.equals("GET") && this.elCuerpo != null ) {
                Log.d("clienterestandroid", "doInBackground(): no es get, pongo cuerpo");
                connection.setDoOutput(true);
                // si no es GET, pongo el cuerpo que me den en la petición
                DataOutputStream dos = new DataOutputStream (connection.getOutputStream());
                dos.writeBytes(this.elCuerpo);
                dos.flush();
                dos.close();
            }

            // ya he enviado la petición
            Log.d("clienterestandroid", "doInBackground(): petición enviada ");

            // ahora obtengo la respuesta

            int rc = connection.getResponseCode();
            String rm = connection.getResponseMessage();
            String respuesta = "" + rc + " : " + rm;
            Log.d("clienterestandroid", "doInBackground() recibo respuesta = " + respuesta);
            this.codigoRespuesta = rc;

            try {

                InputStream is = connection.getInputStream();
                BufferedReader br = new BufferedReader(new InputStreamReader(is));

                Log.d("clienterestandroid", "leyendo cuerpo");
                StringBuilder acumulador = new StringBuilder ();
                String linea;
                while ( (linea = br.readLine()) != null) {
                    Log.d("clienterestandroid", linea);
                    acumulador.append(linea);
                }
                Log.d("clienterestandroid", "FIN leyendo cuerpo");

                this.cuerpoRespuesta = acumulador.toString();
                Log.d("clienterestandroid", "cuerpo recibido=" + this.cuerpoRespuesta);

                connection.disconnect();

            } catch (IOException ex) {
                // dispara excepción cuando la respuesta REST no tiene cuerpo y yo intento getInputStream()
                Log.d("clienterestandroid", "doInBackground() : parece que no hay cuerpo en la respuesta");
            }

            return true; // doInBackground() termina bien

        } catch (Exception ex) {
            Log.d("clienterestandroid", "doInBackground(): ocurrio alguna otra excepcion: " + ex.getMessage());
        }

        return false; // doInBackground() NO termina bien
    } // ()

    // --------------------------------------------------------------------
    // comoFue: T/F --> onPostExecute() -->   (privado: lo llama AsyncTask al terminar)
    // --------------------------------------------------------------------
    protected void onPostExecute(Boolean comoFue) {
        // llamado tras doInBackground()
        Log.d("clienterestandroid", "onPostExecute() comoFue = " + comoFue);
        this.laRespuesta.callback(this.codigoRespuesta, this.cuerpoRespuesta);
    }

} // class


