<%-- Copyright (c) 2013 NuoDB, Inc. --%>
<%@page contentType="text/html" pageEncoding="UTF-8"%>
<%@taglib prefix="t" tagdir="/WEB-INF/tags"%>

<t:page>
    <div id="checkout" class="row">
        <div id="cart"></div>        
        <script id="tpl-cart" type="text/template">    
	        <!--  Cart contents -->
    	    <div class="span9">
                {{#if result}}
                    <form method="post">
						<input type="hidden" name="action" value="update" />
						<table class="table table-hover table-bordered">
	                        <thead>
    	                        <tr>
        	                        <th>Items to buy now</th>
            	                    <th class="text-right">Price</th>
                	                <th class="text-right">Quantity</th>
                    	        </tr>
	                        </thead>
    	                    <tbody>
        	                    {{#result}}
            	                    <tr>
                	                    <td>
                    	                    <div class="media">
                        	                    <a class="pull-left" href="product?productId={{product.id}}"><img class="img-rounded pull-left" src="{{productImage product.imageUrl}}" width="130" /></a>
                                	            <div class="media-body">
                                    	            <h4 class="media-heading">{{product.name}}</h4>
													<div class="prod-metadata">
														<div class="desc">{{{product.description}}}</div>
													</div>
    	                                        </div>
        	                                </div>    
            	                        </td>
                	                    <td class="price text-right">{{priceFormat product.unitPrice}}</td>
                    	                <td class="text-right"><input class="input-mini" type="number" name="product-{{product.id}}" value="{{quantity}}" min="0" max="9999" step="1" /></td>
	                                </tr>
    	                        {{/result}}
        	                </tbody>
                    	</table>
						<p class="text-right">
							<button class="btn" type="submit">Update Cart</button>
						</p>
					</form>
                {{else}}
                    <div class="alert">Your cart is empty.</div>
                {{/if}}
	        </div>
        
    	    <!--  Checkout sidebar -->
        	<div class="span3">
				{{#if result}}
            		<form class="add-to-cart form-inline alert alert-info" method="post">
						<input type="hidden" name="action" value="checkout" />
	                	<label for="quantity">Total: </label>
    	            	<h4 class="price">{{priceFormat totalPrice}}</h4>
        	        	<br />
            	    	<button class="btn btn-success btn-large" type="submit">
                	    	<i class="icon icon-circle-arrow-right icon-white"></i> Check Out
	                	</button>
    	        	</form>
				{{/if}}
       	    	<p>
           	    	<a href="products">Continue Shopping</a>
            	</p>
	        </div>
		</script>
    </div>
</t:page>
