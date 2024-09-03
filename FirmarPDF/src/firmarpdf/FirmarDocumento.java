
package firmarpdf;

import com.google.zxing.BarcodeFormat;
import com.google.zxing.EncodeHintType;
import com.google.zxing.qrcode.QRCodeWriter;
import com.google.zxing.common.BitMatrix;
import com.itextpdf.text.DocumentException;
import com.itextpdf.text.Image;
import com.itextpdf.text.Rectangle;
import com.itextpdf.text.pdf.PdfReader;
import com.itextpdf.text.pdf.PdfStamper;
import com.itextpdf.text.pdf.security.BouncyCastleDigest;
import com.itextpdf.text.pdf.security.MakeSignature;
import com.itextpdf.text.pdf.PdfSignatureAppearance;
import com.itextpdf.text.pdf.security.PrivateKeySignature;
import com.itextpdf.text.pdf.security.ExternalDigest;
import com.itextpdf.text.pdf.security.ExternalSignature;

import com.itextpdf.text.pdf.*;
import com.itextpdf.text.*;
import com.itextpdf.text.pdf.security.*;
import java.io.*;
import java.security.*;
import java.security.cert.Certificate;
import org.bouncycastle.jce.provider.BouncyCastleProvider;


import java.awt.image.BufferedImage;
import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.io.IOException;
import java.security.GeneralSecurityException;
import java.security.KeyStore;
import java.security.PrivateKey;
import java.security.cert.Certificate;
import java.security.cert.X509Certificate;
import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.HashMap;
import java.util.Hashtable;
import java.util.Map;
import java.util.concurrent.ThreadLocalRandom;
import javax.security.auth.x500.X500Principal;


public class FirmarDocumento {
    
    public static String[] FirmarDocmuentosPDF(String[] args) {
        String[] Resp = new String[6];
        if(args==null || args.length < 7)
        {
            Resp[0] = "-2";
            Resp[1] = "Parametros Incompletos para firmar";    
            return Resp;                      
        }
         try {
            // Primera firma
            signPdf(args[1], args[2], args[3], args[4],Integer.parseInt(args[5]),Integer.parseInt(args[6]));
            Resp[0] = "1";
            Resp[1] = "Documento Firmado";
            Resp[2] = args[1];
            Resp[3] = args[2];
            Resp[4] = args[3];
            Resp[5] = args[4];
            return Resp;
        } catch (Exception e) {
         //   e.printStackTrace();
            Resp[0] = "-1";
            Resp[1] = e.toString();
            Resp[2] = "REVISAR EL ARCHIVO P12";
            return Resp;
        }        
    }
    public static void signPdf(String keystorePath, String keystorePassword,String src, String dest, Integer x, Integer y) 
            throws GeneralSecurityException, IOException, DocumentException, Exception {

        KeyStore ks = KeyStore.getInstance("PKCS12");
        // System.out.println(ks);
        ks.load(new FileInputStream(keystorePath), keystorePassword.toCharArray());
        String alias = ks.aliases().nextElement();
        PrivateKey pk = (PrivateKey) ks.getKey(alias, keystorePassword.toCharArray());
        Certificate[] chain = ks.getCertificateChain(alias);
        
        Map DataSignature = getCommonName(keystorePath,keystorePassword);
        
        PdfReader reader = new PdfReader(src);
        FileOutputStream os = new FileOutputStream(dest);
        PdfStamper stamper = PdfStamper.createSignature(reader, os, '\0', null, true);

        PdfSignatureAppearance appearance = stamper.getSignatureAppearance();
       // appearance.setReason("Razón de la firma");
       // appearance.setLocation("Ubicación de la firma");
        SimpleDateFormat dateFormat = new SimpleDateFormat("dd/MM/yyyy");
        String FechaActual = dateFormat.format(new Date());
        
        Rectangle signatureRect = new Rectangle(x, y, x + 150, y + 50);
        String fieldName = FechaActual+"-"+cadenaAleatoria(7);
        appearance.setVisibleSignature(signatureRect, 1, fieldName);
        
        SimpleDateFormat dateFormat2 = new SimpleDateFormat("dd/MM/yyyy HH:mm:ss");
        String currentDate = dateFormat2.format(new Date());

        String qrText = "PROPIETARIO:"+DataSignature.get("CN")+"\nENTIDAD CERT.:"+DataSignature.get("O")+"\nFECHA DE FIRMA: " + currentDate;
        BufferedImage qrImage = generateQRCode(qrText, 150, 50);
        Image qr = Image.getInstance(qrImage, null);
        qr.setAbsolutePosition(x-95, y-10); // Posicionar el QR
        qr.scaleToFit(150, 50); // Ajustar el tamaño del QR

        PdfContentByte content = stamper.getOverContent(1); // Obtener el contenido de la página
        content.addImage(qr);
        
        ExternalDigest digest = new BouncyCastleDigest();
        ExternalSignature signature = new PrivateKeySignature(pk, "SHA-256", null);

        MakeSignature.signDetached(
                appearance,
                digest,
                signature,
                chain,
                null, // CRL
                null, // OCSP
                null, // TSA
                0,    // timestamp
                MakeSignature.CryptoStandard.CMS
        );

        stamper.close();
        reader.close();
    }
    
     // Método para generar el código QR
    private static BufferedImage generateQRCode(String qrText, int width, int height) throws Exception {
        Hashtable<EncodeHintType, String> hintMap = new Hashtable<>();
        hintMap.put(EncodeHintType.CHARACTER_SET, "UTF-8");
        QRCodeWriter qrCodeWriter = new QRCodeWriter();
        BitMatrix bitMatrix = qrCodeWriter.encode(qrText, BarcodeFormat.QR_CODE, width, height, hintMap);

        BufferedImage image = new BufferedImage(width, height, BufferedImage.TYPE_INT_RGB);
        for (int x = 0; x < width; x++) {
            for (int y = 0; y < height; y++) {
                image.setRGB(x, y, bitMatrix.get(x, y) ? java.awt.Color.BLACK.getRGB() : java.awt.Color.WHITE.getRGB());
            }
        }
        return image;
    }
    
    public static Map getCommonName(String keystorePath, String keystorePassword) throws Exception {
        // Cargar el KeyStore
        KeyStore ks = KeyStore.getInstance("PKCS12");
        ks.load(new FileInputStream(keystorePath), keystorePassword.toCharArray());

        // Obtener el alias del certificado
        String alias = ks.aliases().nextElement();

        // Obtener el certificado
        Certificate cert = ks.getCertificate(alias);

        if (cert instanceof X509Certificate) {
            X509Certificate x509Cert = (X509Certificate) cert;

            // Obtener el Subject Principal
            X500Principal subjectPrincipal = x509Cert.getSubjectX500Principal();
            // Extraer el Common Name (CN)           
            
            
            Map<String, String> DataSignature = new HashMap<>();
            String subjectDN = subjectPrincipal.getName();
            int Count = 0;
            String[] dnAttributes = subjectDN.split(",");
             for (String attribute : dnAttributes) {
                 String[] Data = attribute.split("=");
                 if(Data.length>1)
                 {
                    DataSignature.put(Data[0],Data[1]);
                    Count++;
                 }
                //if (attribute.trim().startsWith("=")) {
                    //return attribute.trim().substring(3);  // Retornar el valor del CN
                //}
            }
            return DataSignature;
        }

        return null; // Retornar null si no se encuentra el CN
    }
    
    public static String cadenaAleatoria(int longitud) 
    {
        // El banco de caracteres
        String banco = "abcdefghijklmnopqrstuvwxyzABCDEFGHIJKLMNOPQRSTUVWXYZ1234567890";
        // La cadena en donde iremos agregando un carácter aleatorio
        String cadena = "";
        for (int x = 0; x < longitud; x++) {
            int indiceAleatorio = numeroAleatorioEnRango(0, banco.length() - 1);
            char caracterAleatorio = banco.charAt(indiceAleatorio);
            cadena += caracterAleatorio;
        }
        return cadena;
    }
    public static int numeroAleatorioEnRango(int minimo, int maximo) 
    {
        // nextInt regresa en rango pero con límite superior exclusivo, por eso sumamos 1
        return ThreadLocalRandom.current().nextInt(minimo, maximo + 1);
    }
    
}
