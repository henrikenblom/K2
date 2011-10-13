/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */
package com.imprima.k2.communication;

import com.google.gson.Gson;
import com.imprima.k2.datastore.Order;
import com.imprima.k2.datastore.ProductionDatastore;
import java.io.IOException;
import java.io.PrintWriter;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.Locale;
import javax.servlet.ServletException;
import javax.servlet.http.HttpServlet;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

/**
 *
 * @author henrik
 */
public class OrderServlet extends HttpServlet {

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

                if (action.equalsIgnoreCase("get_orders_by_username")
                        && request.getParameter("username") != null) {

                    getOrdersByUsername(out, request.getParameter("username"));

                } else if (action.equalsIgnoreCase("get_order_by_ordernumber")
                        && request.getParameter("ordernumber") != null) {
                    
                    getOrder(out, Integer.parseInt(request.getParameter("ordernumber")));
                    
                }

            }

        } finally {

            out.close();

        }

    }

    private void getOrder(PrintWriter out, Integer ordernumber) {

        gson.toJson(productionDatastore.getOrder(ordernumber).toMap(new Locale("sv")), out);

    }

    private void getOrdersByUsername(PrintWriter out, String username) {

        ArrayList<HashMap<String, String>> orderArray = new ArrayList<HashMap<String, String>>();

        for (Order order : productionDatastore.getOrderListByUsername(username)) {

            orderArray.add(order.toMap(new Locale("sv")));

        }

        gson.toJson(orderArray, out);

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
