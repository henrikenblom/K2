/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */
package com.imprima.k2.communication;

import com.imprima.k2.datastore.ProductionDatastore;
import com.imprima.kesession.UserSessionController;
import com.imprima.level9.Message;
import com.imprima.level9.UserMessage;
import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.util.logging.Level;
import java.util.logging.Logger;
import javax.servlet.ServletException;
import javax.servlet.http.HttpServlet;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import org.apache.commons.fileupload.FileItemIterator;
import org.apache.commons.fileupload.FileItemStream;
import org.apache.commons.fileupload.FileUploadException;
import org.apache.commons.fileupload.servlet.ServletFileUpload;
import org.apache.commons.fileupload.util.Streams;

/**
 *
 * @author henrik
 */
public class UploadServlet extends HttpServlet {

    private UserSessionController userSessionController = UserSessionController.getInstance();
    private ProductionDatastore productionDatastore = ProductionDatastore.getInstance();
    private ServletFileUpload upload = new ServletFileUpload();

    @Override
    public void init() throws ServletException {

        upload.setSizeMax(8589934592L);
        super.init();

    }

    /** 
     * Processes requests for both HTTP <code>GET</code> and <code>POST</code> methods.
     * @param request servlet request
     * @param response servlet response
     * @throws ServletException if a servlet-specific error occurs
     * @throws IOException if an I/O error occurs
     */
    protected void processRequest(HttpServletRequest request, HttpServletResponse response)
            throws ServletException, IOException {

        long start = System.currentTimeMillis();
        
        Integer ordernumber = null;
        String filename = null;

        response.setCharacterEncoding("UTF8");
        request.setCharacterEncoding("UTF8");

        if (ServletFileUpload.isMultipartContent(request)) {

            try {

                FileItemIterator iter = upload.getItemIterator(request);

                while (iter.hasNext()) {

                    FileItemStream item = iter.next();
                    String variableName = item.getFieldName();
                    InputStream stream = item.openStream();

                    if (item.isFormField()) {

                        if (variableName.equals("ordernumber")) {

                            ordernumber = Integer.parseInt(Streams.asString(stream));

                        }

                    } else {

                        String[] path = item.getName().split("\\\\");

                        filename = path[path.length - 1];
                                                
                        userSessionController.publishMessageToUsers(new UserMessage("'" + filename + "' laddas upp till order " + ordernumber + "."),
                                productionDatastore.getUsernameSetByOrdernumer(ordernumber));

                        FileOutputStream outStream = new FileOutputStream(new File("/Users/henrik/Desktop/" + filename));

                        byte[] buffer = new byte[8192];
                        int length;
                        while ((length = stream.read(buffer)) > 0) {
                            outStream.write(buffer, 0, length);
                        }

                        outStream.close();
                        buffer = null;
                        
                        userSessionController.publishMessageToUsers(new UserMessage("'" + filename + "' har laddats upp till order " + ordernumber + "."),
                                productionDatastore.getUsernameSetByOrdernumer(ordernumber));

                    }

                }

            } catch (FileUploadException ex) {
                Logger.getLogger(UploadServlet.class.getName()).log(Level.SEVERE, null, ex);
            } finally {
            }

        }

    }

    // <editor-fold defaultstate="collapsed" desc="HttpServlet methods. Click on the + sign on the left to edit the code.">
    /** 
     * Handles the HTTP <code>GET</code> method.
     * @param request servlet request
     * @param response servlet response
     * @throws ServletException if a servlet-specific error occurs
     * @throws IOException if an I/O error occurs
     */
    @Override
    protected void doGet(HttpServletRequest request, HttpServletResponse response)
            throws ServletException, IOException {
        processRequest(request, response);
    }

    /** 
     * Handles the HTTP <code>POST</code> method.
     * @param request servlet request
     * @param response servlet response
     * @throws ServletException if a servlet-specific error occurs
     * @throws IOException if an I/O error occurs
     */
    @Override
    protected void doPost(HttpServletRequest request, HttpServletResponse response)
            throws ServletException, IOException {
        processRequest(request, response);
    }

    /** 
     * Returns a short description of the servlet.
     * @return a String containing servlet description
     */
    @Override
    public String getServletInfo() {
        return "Short description";
    }// </editor-fold>
}
