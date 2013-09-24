/* Copyright (c) 2013 NuoDB, Inc. */

package com.nuodb.storefront.api;

import java.math.BigDecimal;
import java.util.List;

import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import javax.ws.rs.FormParam;
import javax.ws.rs.GET;
import javax.ws.rs.POST;
import javax.ws.rs.Path;
import javax.ws.rs.PathParam;
import javax.ws.rs.Produces;
import javax.ws.rs.QueryParam;
import javax.ws.rs.core.Context;
import javax.ws.rs.core.MediaType;
import javax.ws.rs.core.Response;
import javax.ws.rs.core.Response.Status;

import com.googlecode.genericdao.search.SearchResult;
import com.nuodb.storefront.exception.ProductNotFoundException;
import com.nuodb.storefront.model.dto.ProductFilter;
import com.nuodb.storefront.model.entity.Customer;
import com.nuodb.storefront.model.entity.Product;
import com.nuodb.storefront.model.entity.ProductReview;
import com.nuodb.storefront.model.type.ProductSort;
import com.nuodb.storefront.servlet.BaseServlet;

@Path("/products")
public class ProductsApi extends BaseApi {
    public ProductsApi() {
    }

    @GET
    @Produces(MediaType.APPLICATION_JSON)
    public SearchResult<Product> search(@Context HttpServletRequest req,
            @QueryParam("matchText") String matchText, @QueryParam("categories") List<String> categories,
            @QueryParam("page") Integer page, @QueryParam("pageSize") Integer pageSize, @QueryParam("sort") ProductSort sort) {
        ProductFilter filter = new ProductFilter(matchText, categories, page, pageSize, sort);
        req.getSession().setAttribute(BaseServlet.SESSION_PRODUCT_FILTER, filter);
        return getService().getProducts(filter);
    }

    @GET
    @Path("/{productId}")
    @Produces(MediaType.APPLICATION_JSON)
    public Response get(@PathParam("productId") int productId) {
        try {
            Product product = getService().getProductDetails(productId);
            return Response.ok(product).build();
        } catch (ProductNotFoundException e) {
            return Response.status(Status.NOT_FOUND).build();
        }
    }

    @POST
    @Produces(MediaType.APPLICATION_JSON)
    public Product addProduct(@QueryParam("name") String name, @FormParam("description") String description, @FormParam("imageUrl") String imageUrl,
            @FormParam("unitPrice") BigDecimal unitPrice, @FormParam("category") List<String> categories) {
        return getService().addProduct(name, description, imageUrl, unitPrice, categories);
    }

    @POST
    @Path("/{productId}/reviews")
    @Produces(MediaType.APPLICATION_JSON)
    public ProductReview addReview(@Context HttpServletRequest req, @Context HttpServletResponse resp,
            @PathParam("productId") int productId, @FormParam("title") String title,
            @FormParam("comments") String comments, @FormParam("emailAddress") String emailAddress, @FormParam("rating") int rating) {
        Customer customer = getOrCreateCustomer(req, resp);
        return getService().addProductReview(customer.getId(), productId, title, comments, emailAddress, rating);
    }
}