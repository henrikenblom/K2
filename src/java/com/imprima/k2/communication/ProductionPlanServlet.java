/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */
package com.imprima.k2.communication;

import com.google.gson.Gson;
import com.imprima.k2.datastore.ProductionDatastore;
import com.imprima.k2.datastore.ProductionPlan;
import java.io.IOException;
import java.io.PrintWriter;
import java.util.Locale;
import javax.servlet.ServletException;
import javax.servlet.http.HttpServlet;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

/**
 *
 * @author henrik
 */
public class ProductionPlanServlet extends HttpServlet {

    private ProductionDatastore productionDatastore = ProductionDatastore.getInstance();
    private Gson gson = new Gson();

    /** 
     * Processes requests for both HTTP <code>GET</code> and <code>POST</code> methods.
     * @param request servlet request
     * @param response servlet response
     * @throws ServletException if a servlet-specific error occurs
     * @throws IOException if an I/O error occurs
     */
    protected void processRequest(HttpServletRequest request, HttpServletResponse response)
            throws ServletException, IOException {

        response.setContentType("application/json;charset=UTF-8");

        PrintWriter out = response.getWriter();

        try {

            if (request.getParameter("action") != null) {

                String action = request.getParameter("action");

                if (action.equalsIgnoreCase("get_productionplan_by_ordernumber")
                        && request.getParameter("ordernumber") != null) {

                    getProductionPlan(out, Integer.parseInt(request.getParameter("ordernumber")));

                } else if (action.equalsIgnoreCase("order_has_productionplan")
                        && request.getParameter("ordernumber") != null) {
                    
                    gson.toJson(productionDatastore.containsProductionPlan(Integer.parseInt(request.getParameter("ordernumber"))), out);
                    
                }

            }

        } finally {
            out.close();
        }

    }

    private void getProductionPlan(PrintWriter out, int ordernumber) {

        ProductionPlan productionPlan = productionDatastore.getProductionPlan(ordernumber);
        
            gson.toJson(productionPlan.toList(new Locale("sv"), true), out);


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
