package com.nuodb.storefront.api;

import javax.ws.rs.CookieParam;
import javax.ws.rs.FormParam;
import javax.ws.rs.GET;
import javax.ws.rs.POST;
import javax.ws.rs.PUT;
import javax.ws.rs.Path;
import javax.ws.rs.Produces;
import javax.ws.rs.core.MediaType;
import javax.ws.rs.core.NewCookie;
import javax.ws.rs.core.Response;

import com.googlecode.genericdao.search.SearchResult;
import com.nuodb.storefront.model.Cart;
import com.nuodb.storefront.model.CartSelection;
import com.nuodb.storefront.model.Customer;
import com.nuodb.storefront.model.Transaction;

@Path("/customer")
public class CustomerApi extends BaseApi {
    public CustomerApi() {
    }

    @GET
    @Path("/cart")
    @Produces(MediaType.APPLICATION_JSON)
    public Response getCartSelections(@CookieParam("customerId") String customerId) {
        Customer customer = getOrCreateCustomer(customerId);
        Cart result = getService().getCustomerCart(customer.getId());
        return Response.ok(result).cookie(new NewCookie("customerId", String.valueOf(customer.getId()))).build();
    }

    @PUT
    @Path("/cart")
    @Produces(MediaType.APPLICATION_JSON)
    public Response addToCart(@CookieParam("customerId") String customerId, @FormParam("productId") int productId, @FormParam("quantity") int quantity) {
        Customer customer = getOrCreateCustomer(customerId);
        int itemCount = getService().addToCart(customer.getId(), productId, quantity);

        SearchResult<CartSelection> result = new SearchResult<CartSelection>();
        result.setTotalCount(itemCount);
        return Response.ok(result).cookie(new NewCookie("customerId", String.valueOf(customer.getId()))).build();
    }

    @POST
    @Path("/checkout")
    @Produces(MediaType.APPLICATION_JSON)
    public Response purchase(@CookieParam("customerId") String customerId) {
        Customer customer = getOrCreateCustomer(customerId);
        Transaction transaction = getService().checkout(customer.getId());
        return Response.ok(transaction).cookie(new NewCookie("customerId", String.valueOf(customer.getId()))).build();
    }
}
