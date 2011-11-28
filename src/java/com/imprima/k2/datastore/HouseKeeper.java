package com.imprima.k2.datastore;

import com.google.gson.Gson;
import java.io.IOException;
import java.io.PrintWriter;
import java.sql.SQLException;
import java.util.concurrent.ScheduledThreadPoolExecutor;
import java.util.concurrent.TimeUnit;
import javax.servlet.ServletException;
import javax.servlet.http.HttpServlet;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

/**
 *
 * @author henrik
 * 
 * Class to perform housekeeping tasks such as database synchronization.
 * 
 * This servlet also contains methods for setting and getting system values.
 * 
 */
public class HouseKeeper extends HttpServlet {

    private ScheduledThreadPoolExecutor scheduledThreadPoolExecutor = new ScheduledThreadPoolExecutor(2);
    private int userDBSyncInterval = 10;
    private int preemptiveCachingInterval = 7;
    private GKSSynchronizationTask preemptiveCachingTask;
    private UserDBSyncTask userDBSyncTask;
    private Gson gson = new Gson();

    @Override
    public void init() throws ServletException {

        try {

            preemptiveCachingTask = new GKSSynchronizationTask();
            userDBSyncTask = new UserDBSyncTask();

            startTimers();

        } catch (Exception ex) {
            throw new ServletException(ex);
        }

        super.init();

    }

    @Override
    public void destroy() {

        stopTimers();

        preemptiveCachingTask = null;
        userDBSyncTask = null;

        super.destroy();

    }

    private void startTimers() {

        scheduledThreadPoolExecutor.scheduleWithFixedDelay(userDBSyncTask, 0l, userDBSyncInterval, TimeUnit.SECONDS);
        scheduledThreadPoolExecutor.scheduleWithFixedDelay(preemptiveCachingTask, 0l, preemptiveCachingInterval, TimeUnit.SECONDS);

    }

    private void stopTimers() {

        scheduledThreadPoolExecutor.shutdown();

    }

    private void restartTimers() throws SQLException, ClassNotFoundException {

        stopTimers();
        startTimers();

    }

    private int getPreemptiveCachingInterval() {
        return preemptiveCachingInterval;
    }

    private void setPreemptiveCachingInterval(int preemptiveCachingInterval) {
        this.preemptiveCachingInterval = preemptiveCachingInterval;
    }

    private int getUserDBSyncInterval() {
        return userDBSyncInterval;
    }

    private void setUserDBSyncInterval(int userDBSyncInterval) {
        this.userDBSyncInterval = userDBSyncInterval;
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

        response.setContentType("text/json;charset=UTF-8");

        PrintWriter out = response.getWriter();

        if (request.getParameter("action") != null) {

            if (request.getParameter("action").equalsIgnoreCase("restart")) {

                try {
                    restartTimers();
                } catch (Exception ex) {
                    throw new ServletException(ex);
                }

            } else if (request.getParameter("action").equalsIgnoreCase("setUserDBSyncInterval")) {

                setUserDBSyncInterval(Integer.parseInt(request.getParameter("value")));

            } else if (request.getParameter("action").equalsIgnoreCase("setPreemptiveCachingInterval")) {

                setPreemptiveCachingInterval(Integer.parseInt(request.getParameter("value")));

            } else if (request.getParameter("action").equalsIgnoreCase("getPreemptiveCachingInterval")) {

                out.print(gson.toJson(getPreemptiveCachingInterval()));

            } else if (request.getParameter("action").equalsIgnoreCase("getUserDBSyncInterval")) {

                out.print(gson.toJson(getUserDBSyncInterval()));

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
