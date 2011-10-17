/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */
package com.imprima.k2.communication;

import com.imprima.k2.datastore.ProductionDatastore;
import com.imprima.kesession.UserSessionController;
import com.imprima.level9.UserMessage;
import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.util.logging.Level;
import java.util.logging.Logger;
import javax.servlet.ServletConfig;
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

    private static final int DEFAULTCHUNKSIZE = 8192;
    private static final long DEFAULTMAXIMUMFILESIZE = 8589934592L;
    private UserSessionController userSessionController = UserSessionController.getInstance();
    private ProductionDatastore productionDatastore = ProductionDatastore.getInstance();
    private ServletFileUpload servletUpload = new ServletFileUpload();
    private String temporaryDirectory = "/tmp";
    private int chunkSize = DEFAULTCHUNKSIZE;

    @Override
    public void init(ServletConfig config) throws ServletException {

        try {

            setTemporaryDirectory(config.getInitParameter("temporaryDirectory"));

        } catch (Exception ex) {
            ex.printStackTrace();
        }

        try {

            servletUpload.setSizeMax(Long.parseLong(config.getInitParameter("maximumFileSize")));

        } catch (Exception ex) {

            Logger.getLogger(Logger.GLOBAL_LOGGER_NAME).log(Level.WARNING, "Using default maximum file size of " + DEFAULTMAXIMUMFILESIZE + " bytes.");

        }

        try {

            setChunkSize(Integer.parseInt(config.getInitParameter("chunkSize")));

        } catch (Exception ex) {

            Logger.getLogger(Logger.GLOBAL_LOGGER_NAME).log(Level.WARNING, "Using default chunk size of " + DEFAULTCHUNKSIZE + " bytes.");

        }

        super.init(config);

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

        Integer ordernumber = null;
        String originalFilename = null;
        FileOutputStream fileOutputStream = null;
        File outputFile = null;
        byte[] buffer = null;
        InputStream inputStream = null;

        response.setCharacterEncoding("UTF8");
        request.setCharacterEncoding("UTF8");

        if (ServletFileUpload.isMultipartContent(request)) {

            try {

                FileItemIterator iter = servletUpload.getItemIterator(request);

                while (iter.hasNext()) {

                    FileItemStream item = iter.next();
                    String variableName = item.getFieldName();
                    inputStream = item.openStream();

                    if (item.isFormField()) {

                        if (variableName.equals("ordernumber")) {

                            ordernumber = Integer.parseInt(Streams.asString(inputStream));

                        }

                    } else {

                        String[] path = item.getName().split("\\\\");

                        originalFilename = path[path.length - 1];

                        path = null;

                        userSessionController.publishMessageToUsers(new UserMessage("'" + originalFilename + "' laddas upp till order " + ordernumber + "."),
                                productionDatastore.getUsernameSetByOrdernumer(ordernumber));

                        outputFile = new File(getTemporaryDirectory() + "/" + originalFilename);
                        fileOutputStream = new FileOutputStream(outputFile);

                        buffer = new byte[chunkSize];
                        int length;
                        while ((length = inputStream.read(buffer)) > 0) {
                            fileOutputStream.write(buffer, 0, length);
                        }

                        userSessionController.publishMessageToUsers(new UserMessage("'" + originalFilename + "' har laddats upp till order " + ordernumber + "."),
                                productionDatastore.getUsernameSetByOrdernumer(ordernumber));

                    }

                }

            } catch (FileUploadException ex) {
                Logger.getLogger(UploadServlet.class.getName()).log(Level.SEVERE, null, ex);
            } finally {

                if (inputStream != null) {

                    try {
                        inputStream.close();
                    } catch (Exception ex) {
                        //no-op
                    }

                    inputStream = null;

                }

                if (fileOutputStream != null) {

                    try {
                        fileOutputStream.close();
                    } catch (Exception ex) {
                        //no-op
                    }

                    fileOutputStream = null;

                }

                buffer = null;
                outputFile = null;

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

    private String getTemporaryDirectory() {
        return temporaryDirectory;
    }

    private void setTemporaryDirectory(String temporaryDirectory) throws Exception {

        if (temporaryDirectory == null) {
            
            throw new Exception("temporaryDirectory can not be 'null'. Now set to '" + getTemporaryDirectory() + "'");
            
        } else {
            
            this.temporaryDirectory = temporaryDirectory;
            Logger.getLogger(Logger.GLOBAL_LOGGER_NAME).log(Level.INFO, "Temporary directory set to '" + temporaryDirectory + "'.");
            
        }

    }

    private void setMaximumFileSize(long maximumFileSize) {

        if (maximumFileSize > 1073741824L) {

            servletUpload.setFileSizeMax(maximumFileSize);
            Logger.getLogger(Logger.GLOBAL_LOGGER_NAME).log(Level.INFO, "Maximum file size set to " + maximumFileSize + " bytes.");

        } else {

            Logger.getLogger(Logger.GLOBAL_LOGGER_NAME).log(Level.WARNING, "Requested maximum file size is too small. Using default maximum file size of " + DEFAULTMAXIMUMFILESIZE + " bytes.");
            servletUpload.setFileSizeMax(DEFAULTMAXIMUMFILESIZE);

        }

    }

    private void setChunkSize(int chunkSize) {

        if (chunkSize > 1024) {

            this.chunkSize = chunkSize;
            Logger.getLogger(Logger.GLOBAL_LOGGER_NAME).log(Level.INFO, "Chunk size set to " + chunkSize + " bytes.");

        } else {

            Logger.getLogger(Logger.GLOBAL_LOGGER_NAME).log(Level.WARNING, "Requested chunk size is too small. Using default chunk size of " + DEFAULTCHUNKSIZE + " bytes.");
            this.chunkSize = DEFAULTCHUNKSIZE;

        }

    }
}
