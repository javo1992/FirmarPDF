/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package firmarpdf;

import java.io.FileInputStream;
import java.security.KeyStore;
import java.security.PrivateKey;
import java.security.cert.Certificate;
import java.security.cert.X509Certificate;

/**
 *
 * @author usuario
 */
public class ValidadorFirma {
    public  static String[] ValidarFirmaP12(String[] Data)
    {
        
        String[] Resp = new String[4];
        if(Data==null || Data.length < 3)
        {
            Resp[0] = "-2";
            Resp[1] = "Parametros Incompletos";    
            return Resp;                      
        }
        
        Boolean paso1 = isValidP12(Data[1],Data[2]);
        if(!paso1)
        {
            Resp[0] = "-1";
            Resp[1] = "Error al cargar - Password o Ruta P12 incorrecta";
            Resp[2] = Data[1];
            Resp[3] = Data[2];
            return Resp; 
        }
        Boolean paso2 = hasPrivateKey(Data[1],Data[2]);
        if(!paso2)
        {
            Resp[0] = "-1";
            Resp[1] = "Error al verificar la clave privada"; 
            Resp[2] = Data[1];
            Resp[3] = Data[2];
            return Resp; 
        }
         Boolean paso3 = isCertificateValid(Data[1],Data[2]);
        if(!paso2)
        {
            Resp[0] = "-1";
            Resp[1] = "Error al verificar la validez del certificado - Expirado";
            Resp[2] = Data[1];
            Resp[3] = Data[2];
            return Resp; 
        }
        
        Resp[0] = "1";
        Resp[1] = "Firma digital Valida";
        Resp[2] = Data[1];
        Resp[3] = Data[2];
        
        
        return Resp;               
    }    
    public static boolean isValidP12(String p12Path, String password) {
        try {
            // Cargar el archivo .p12
            KeyStore ks = KeyStore.getInstance("PKCS12");
            ks.load(new FileInputStream(p12Path), password.toCharArray());

            // Si el archivo se carga correctamente, es válido
            return true;
        } catch (Exception e) {
            // Si hay alguna excepción, el archivo .p12 no es válido
            //System.out.println("Error al cargar el archivo .p12: " + e.getMessage());
            return false;
        }
        
    }
    
    public static boolean hasPrivateKey(String p12Path, String password) {
        try {
            // Cargar el archivo .p12
            KeyStore ks = KeyStore.getInstance("PKCS12");
            ks.load(new FileInputStream(p12Path), password.toCharArray());

            // Obtener el alias del certificado
            String alias = ks.aliases().nextElement();

            // Intentar obtener la clave privada
            PrivateKey pk = (PrivateKey) ks.getKey(alias, password.toCharArray());

            // Si se obtiene la clave privada, es válida
            return pk != null;
        } catch (Exception e) {
           // System.out.println("Error al verificar la clave privada: " + e.getMessage());
            return false;
        }
    }
     public static boolean isCertificateValid(String p12Path, String password) {
        try {
            // Cargar el archivo .p12
            KeyStore ks = KeyStore.getInstance("PKCS12");
            ks.load(new FileInputStream(p12Path), password.toCharArray());

            // Obtener el alias del certificado
            String alias = ks.aliases().nextElement();

            // Obtener el certificado
            Certificate cert = ks.getCertificate(alias);

            // Verificar si el certificado es válido y no ha expirado
            X509Certificate x509Cert = (X509Certificate) cert;
            x509Cert.checkValidity();

            return true;
        } catch (Exception e) {
            System.out.println("Error al verificar la validez del certificado: " + e.getMessage());
            return false;
        }
    }
}
