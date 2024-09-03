package firmarpdf;
import com.google.gson.Gson;
import static firmarpdf.FirmarDocumento.FirmarDocmuentosPDF;
import static firmarpdf.FirmarDocumento.signPdf;
import static firmarpdf.ValidadorFirma.ValidarFirmaP12;


public class FirmarPDF {

    public static void main(String[] args) {
        //System.out.print(args);
        Gson gson = new Gson();
        if(args.length>0)
        {        
            switch(args[0])
            {
                case "1":
                    //firma pdfs
                    String[] response = FirmarDocmuentosPDF(args);
                    System.out.println(gson.toJson(response));
                break;
                 case "2":
                     //valida si la firma es valida                     
                    String[] response2 = ValidarFirmaP12(args);
                    System.out.println(gson.toJson(response2));                     
                break;
                 case "-info":
                     //datos que se deven enviar         
                    String[] response3 = new String[7];
                    response3[0] = "[0]- (1) firmar pdfs ó (2) validar firma";
                    response3[1] = "[1]- Ruta firma";
                    response3[2] = "[2]- Clave firma";
                    response3[3] = "[3]- Ruta documento pdf en caso de firmar pdfs";
                    response3[4] = "[4]- Ruta final del documento en caso de firmar pdfs";
                    response3[5] = "[5]- posicion (x) en caso de firmar pdfs";
                    response3[6] = "[6]- posicion (y) en caso de firmar pdfs";
                    System.out.println(gson.toJson(response3));                     
                break;
                                
            }
               
        }else
        {
            String[] Resp = new String[3];
            Resp[0] = "-2";
            Resp[1] = "parametero no enviado, envie parametro -info para ver mas detalles ";
            Resp[2] = "";
            System.out.println(gson.toJson(Resp));              
        }
    } 
}

//2 C:\\xampp_8.2\\htdocs\\fpdf_1\\certificados\\EDISON_JAVIER_FARINANGO_CABEZAS_0102024.p12 Fa19071992 C:\\xampp_8.2\\htdocs\\fpdf_1\\lib\\Formato_de_variables.pdf

//2 C:\\xampp_8.2\\htdocs\\fpdf_1\\certificados\\EDISON_JAVIER_FARINANGO_CABEZAS_0102024.p12 Fa19071992 C:\\xampp_8.2\\htdocs\\fpdf_1\\lib\\Formato_de_variables.pdf C:\\xampp_8.2\\htdocs\\fpdf_1\\lib\\Formato_de_variables_firmado.pdf