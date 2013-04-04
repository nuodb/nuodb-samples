package com.nuodb.storefront.servlet;

import java.io.IOException;
import java.util.HashMap;
import java.util.Map;

import javax.servlet.ServletException;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

import com.nuodb.storefront.model.Cart;
import com.nuodb.storefront.model.Customer;
import com.nuodb.storefront.model.MessageSeverity;

public class CartServlet extends BaseServlet {
    private static final long serialVersionUID = 4182284887959608220L;

    /**
     * GET: shows the contents of the cart
     */
    @Override
    protected void doGet(HttpServletRequest req, HttpServletResponse resp) throws ServletException, IOException {
        int customerId = getOrCreateCustomer(req, resp).getId();
        Cart cart = getService().getCustomerCart(customerId);
        showPage(req, resp, "Cart", "cart", cart);
    }

    /**
     * POST: For an "update" action : update cart and show the cart page For a "checkout" action: moves the cart items to a transaction and redirects
     * to the products page with a success message
     */
    @Override
    protected void doPost(HttpServletRequest req, HttpServletResponse resp) throws ServletException, IOException {
        try {
            Customer customer = getOrCreateCustomer(req, resp);
            String action = req.getParameter("action");

            if ("update".equals(action)) {
                Map<Integer, Integer> productQuantityMap = new HashMap<Integer, Integer>();
                for (Map.Entry<String, String[]> param : req.getParameterMap().entrySet()) {
                    if (param.getKey().startsWith("product-")) {
                        int productId = Integer.parseInt(param.getKey().substring(8));
                        int quantity = Integer.parseInt(param.getValue()[0]);
                        productQuantityMap.put(productId, quantity);
                    }
                }

                int cartItemCount = getService().updateCart(customer.getId(), productQuantityMap);
                customer.setCartItemCount(cartItemCount);
                doGet(req, resp);
            } else if ("checkout".equals(action)) {
                // Move items from cart to transaction
                getService().checkout(customer.getId());

                // Report success
                String itemDesc = (customer.getCartItemCount() != 1) ? customer.getCartItemCount() + " items" : "item";
                addMessage(req, MessageSeverity.SUCCESS, "Your transaction was successful.  Your " + itemDesc
                        + " will be shipped soon.  Thank you for shopping with us!");

                // Forward to products page
                customer.setCartItemCount(0);
                resp.sendRedirect("products");
            }
        } catch (Exception e) {
            addErrorMessage(req, e);
            doGet(req, resp);
        }
    }
}
