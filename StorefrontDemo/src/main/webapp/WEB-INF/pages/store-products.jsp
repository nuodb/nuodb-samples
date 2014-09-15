<%-- Copyright (c) 2013-2014 NuoDB, Inc. --%>
<%@page contentType="text/html" pageEncoding="UTF-8"%>
<%@taglib prefix="t" tagdir="/WEB-INF/tags"%>

<t:page>
    <div class="row">
        <!-- Categories  -->
        <div class="span3 sidebar">
            <h3>Categories</h3>
            <div id="category-nav"></div>
            <script id="tpl-category-nav" type="text/template">
                <ul class="nav nav-tabs nav-stacked">
                    {{#result}}
                        <li data-category="{{name}}">
							<a href="#">
								{{name}}
								<!--<span class="badge pull-right">{{numProducts}}</span>-->
								<button type="button" class="close">&times;</button>
							</a>							
						</li>
                    {{/result}}
                </ul>
            </script>
        </div>

        <div class="span9" id="col-content">
            <!-- Product list filter -->
            <div class="navbar">
                <div class="nav">
                    <p class="navbar-text">Showing <span id="lbl-all-items"></span></p>
                </div>
                <!--
                <div class="btn-group">
                    <button class="btn active" id="lbl-all-items">All items</button>
                    <button class="btn">New</button>
                    <button class="btn">Popular</button>
                    <button class="btn">On sale</button>
                </div>
                 -->
                <div class="nav pull-right">
                    <p class="nav navbar-text">Sort by:</p>
                    <div class="btn-group">
                        <a class="btn dropdown-toggle" data-toggle="dropdown" href="#"><span id="product-sort-label">Relevance</span> <span class="caret"></span></a>
                        <ul class="dropdown-menu pull-right" id="product-sort">
                            <li><a href="#" data-sort="RELEVANCE">Relevance</a></li>
                            <li><a href="#" data-sort="NEW_AND_POPULAR">New and popular</a></li>
                            <li><a href="#" data-sort="PRICE_LOW_TO_HIGH">Price: low to high</a></li>
                            <li><a href="#" data-sort="PRICE_HIGH_TO_LOW">Price: high to low</a></li>
                            <li><a href="#" data-sort="AVG_CUSTOMER_REVIEW">Avg customer review</a></li>
                            <li><a href="#" data-sort="DATE_CREATED">Publication date</a></li>
                        </ul>
                    </div>
                </div>
            </div>
            <div class="clearfix"></div>

            <!-- Product list  -->
            <section>
                <div id="product-list"></div>
                <script id="tpl-product-list" type="text/template">
				{{#if result}}
                    <ul class="thumbnails thumbnails-prod">
                        {{#result}}
                            <li class="span3">
                                <a href="store-product?productId={{id}}" class="thumbnail">
                                    <img src="{{productImage imageUrl}}" />
                                    <div class="prod-metadata">
                                        <div class="name">{{name}}</div>
                                        <div class="price">{{priceFormat unitPrice}}</div>
                                        <div class="rating">
                                            <div class="rateit" data-rateit-value="{{rating}}" data-rateit-ispreset="true" data-rateit-readonly="true"></div>
                                            {{#if reviewCount}}
                                                <span class="review-count">({{reviewCount}})</span>
                                            {{else}}
                                                <span class="review-count">No reviews yet</span>
                                            {{/if}}
                                        </div>
                                    </div>
                                </a>
                            </li>
                        {{/result}}
                    </ul>
				{{else}}
					{{^append}}
						<div class="alert">No products match your criteria.</div>
					{{/append}}
				{{/if}}
                </script>
            </section>

            <!-- Infinite scroll detector -->
            <div id="paginator" class="loading hidden"></div>
        </div>
    </div>
</t:page>
