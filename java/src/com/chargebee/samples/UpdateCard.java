package com.chargebee.samples;

import com.chargebee.Result;
import com.chargebee.models.HostedPage;
import com.chargebee.models.Subscription;
import com.chargebee.samples.common.*;
import java.io.IOException;
import java.io.PrintWriter;
import java.net.URLEncoder;
import javax.servlet.ServletException;
import javax.servlet.http.HttpServlet;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

/*
 * Demo on how to use ChargeBee Update Card Hosted Page API to update card for a customer. 
 * 
 */
public class UpdateCard extends HttpServlet {

    @Override
    protected void doGet(HttpServletRequest request, HttpServletResponse response)
            throws ServletException, IOException {
        String path = request.getServletPath();
        try {
            if (path.endsWith("/update")) {
                updateCardHostedPage(request, response);
            } else if (path.endsWith("/redirect_handler")) {
                redirectFromChargeBee(request, response);
            } else {
                response.sendError(HttpServletResponse.SC_BAD_REQUEST);
            }
        } catch (Exception e) {
            throw new RuntimeException(e);
        }

    }

    /*
     * Redirects the customer to ChargeBee Update Card Hosted Page API.
     */
    protected void updateCardHostedPage(HttpServletRequest request, HttpServletResponse response)
            throws ServletException, IOException {
        /*
         * Calling the ChargeBee Update Card Hosted Page API to update card for 
         * a customer by passing the particular customers customer id.
         * 
         * Note : To use this API return url for Update Card API's page must be set.
         */
        
        Result updateCardResult = HostedPage.updateCard()
                .embed(Boolean.FALSE)
                .customerId(request.getParameter("customer_id"))
                .request();
        
        
        /*
         * Redirecting the customer to the URL returned by ChargeBee.
         */
        
        String updateCardUrl = updateCardResult.hostedPage().url();
        response.sendRedirect(updateCardUrl);
        

    }

    /*
     * Handles the redirection from ChargeBee server.
     */
    protected void redirectFromChargeBee(HttpServletRequest request, HttpServletResponse response)
            throws ServletException, IOException {
        /* The request will have hosted page id and state of the customer card
         * update status.
         */
        
        if ("succeeded".equals(request.getParameter("state"))) {
            /* Request the ChargeBee server about the Update Card Hosted Page status 
             * and details about the subscription and customer.
             */
            Result result = HostedPage.retrieve(request.getParameter("id")).request();
             
            HostedPage hostedPage = result.hostedPage();
            if( !hostedPage.state().equals(HostedPage.State.SUCCEEDED)) {
                response.sendError(HttpServletResponse.SC_BAD_REQUEST);
                return;
            }
            
            
            String customerId = result.hostedPage().content().customer().id();
            String queryParameters = "customer_id=" + URLEncoder.encode(customerId, "UTF-8")
                    + "&updated=" + URLEncoder.encode("true", "UTF-8");
            response.sendRedirect("profile.jsp?" + queryParameters);
            
        } else {
            /* If other than success state is received error page is shown.
             */
            response.sendError(HttpServletResponse.SC_BAD_REQUEST);
        }

    }

    public static Result fetchSubscriptionDetail(HttpServletRequest request) throws IOException{
        
        String id = request.getParameter("customer_id");
        Result subscriptionDetail = Subscription.retrieve(id).request();
        
        
        return subscriptionDetail;
    }
     
    /**
     * Returns a short description of the servlet.
     *
     * @return a String containing servlet description
     */
    @Override
    public String getServletInfo() {
        return "Demo on how to use ChargeBee Update Card Hosted Page API to update card "
                + "for a customer";
    }
}
