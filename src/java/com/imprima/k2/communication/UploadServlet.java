/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */
package com.imprima.k2.communication;

import java.io.IOException;
import java.io.PrintWriter;
import java.util.AbstractMap.SimpleEntry;
import java.util.Map.Entry;
import javax.servlet.ServletException;
import javax.servlet.annotation.MultipartConfig;
import javax.servlet.http.HttpServlet;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import javax.servlet.http.Part;

/**
 *
 * @author henrik
 */
@MultipartConfig(location = "/tmp", fileSizeThreshold = 1024 * 1024)
public class UploadServlet extends HttpServlet {

    /** 
     * Processes requests for both HTTP <code>GET</code> and <code>POST</code> methods.
     * @param request servlet request
     * @param response servlet response
     * @throws ServletException if a servlet-specific error occurs
     * @throws IOException if an I/O error occurs
     */
    protected void processRequest(HttpServletRequest request, HttpServletResponse response)
            throws ServletException, IOException {

        request.setCharacterEncoding("UTF8");

        PrintWriter writer = response.getWriter();

        try {

            for (Part part : request.getParts()) {

                if (getContentDispositionValue(part.getHeader("content-disposition"), "filename") != null) {

                    System.err.println(part.getContentType());
                    System.err.println(part.getSize());
                    System.err.println(getContentDispositionValue(part.getHeader("content-disposition"), "filename"));

                } else {

                    ParameterEntry entry = getParameter(part);
                    
                    System.err.println(entry.getKey() + ": " + entry.getValue());

                }

            }

        } finally {
        }
    }

    private ParameterEntry getParameter(Part part) throws IOException {

        byte[] data = new byte[(int) part.getSize()];

        part.getInputStream().read(data);
        
        return new ParameterEntry(getContentDispositionValue(part.getHeader("content-disposition"), "name"), new String(data, "UTF8"));

    }

    private String getContentDispositionValue(String contentDispositionString, String key) {

        String retval = null;

        if (contentDispositionString.startsWith("form-data;")) {

            for (String entry : contentDispositionString.split(";")) {

                if (entry.trim().startsWith(key)) {

                    retval = entry.split("=")[1].replace("\"", "");
                    break;

                }

            }

        }

        return retval;

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
