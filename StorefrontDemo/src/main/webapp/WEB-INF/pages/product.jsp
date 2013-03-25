<%@page contentType="text/html" pageEncoding="UTF-8"%>
<%@taglib prefix="t" tagdir="/WEB-INF/tags"%>

<t:page>
    <section id="product"></section>
    <script id="tpl-product" type="text/template">
	{{#result}}
        <!-- Product info  -->
        <div id="product-info" class="row">
            <div class="span4">
                <img class="img-polaroid img-rounded" width="300" src="{{productImage imageUrl}}" />
            </div>

            <div class="span5">
                <h2 class="name">{{name}}</h2>
                <p>{{{description}}}</p>
				{{#if categories}}
					<p class="categories">					
						{{#categories}}
  							<a href="products?categories={{urlEncode this}}"><span class="label">{{this}}</span></a>
						{{/categories}}
					</p>
				{{/if}}
                <div class="prod-metadata">
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
            </div>
            <div class="span3">
                <form class="add-to-cart form-inline alert alert-info">
                    <label for="quantity">Quantity: </label> <input class="input-mini" type="number" name="quantity" value="1" min="1" step="1" /> <br />
                    <button class="btn btn-primary btn-large">
                        <i class="icon icon-shopping-cart icon-white"></i> Add to Cart
                    </button>
                </form>
            </div>
        </div>

        <!-- Reviews  -->
        <div id="reviews" class="row">
            <div class="span12">
                <h3>Customer Reviews</h3>

			{{#if reviews}}
				{{#reviews}}
                <div class="media media-review">
                    <div class="pull-left">
                        <img class="media-object img-polaroid img-rounded img-gravitar" src="http://www.gravatar.com/avatar/{{gravitarHash}}?s=64&d=mm" />
                    </div>
                    <div class="media-body">
                        <h4 class="media-heading">
                            <div class="rateit" data-rateit-value="{{rating}}" data-rateit-ispreset="true" data-rateit-readonly="true"></div>
                            {{title}}
                        </h4>
                        <div class="date">{{dateFormat dateAdded}}</div>
                        <div class="byline">
                            By <b>{{customer.displayName}}</b>
                        </div>
                        <p class="comment">{{comments}}</p>
                    </div>
                </div>
				{{/reviews}}
			{{else}}
				<div class="alert alert-info">There are no reviews of this product yet.</div>
			{{/if}}
                <p>
                    Do you have this product? <a href="#dlg-add-review" data-toggle="modal">Submit your review</a>.
                </p>
            </div>
        </div>
	{{/result}}
    </script>

    <!--  "Review this Product" dialog -->
    <form class="add-review" method="POST" >
        <div id="dlg-add-review" class="modal hide fade">
            <div class="modal-header">
                <button type="button" class="close" data-dismiss="modal" aria-hidden="true">&times;</button>
                <h3>Review this Product</h3>
            </div>
            <div class="modal-body form-horizontal">
                <div class="control-group">
                    <label class="control-label" for="inputEmail">Title:</label>
                    <div class="controls">
                        <input class="input-xlarge" type="text" name="title" placeholder="Title">
                    </div>
                </div>
                <div class="control-group">
                    <label class="control-label" for="inputEmail">Rating:</label>
                    <div class="controls">
                        <div class="rateit" data-rateit-ispreset="true" data-rateit-readonly="false" data-rateit-resetable="false" data-rateit-step="1" data-rateit-backingfld="#rating"></div>
                        <input type="hidden" name="rating" id="rating" />
                    </div>
                </div>
                <div class="control-group">
                    <label class="control-label" for="inputEmail">Comments:</label>
                    <div class="controls">
                        <textarea rows="5" class="input-xlarge" name="comments" placeholder="Comments"></textarea>
                    </div>
                </div>
                <div class="control-group">
                    <label class="control-label" for="inputEmail">Email:</label>
                    <div class="controls">
                        <input class="input-xlarge" type="text" name="emailAddress" placeholder="Email address"> <span class="help-block">Used to display your avitar next to your review via <a target="_blank" href="http://en.gravatar.com">Gravitar</a>.
                        </span>
                    </div>
                </div>
            </div>
            <div class="modal-footer">
                <button href="#" class="btn btn-primary" id="btn-submit-review">Submit Review</button>
                <button href="#" class="btn" data-dismiss="modal">Cancel</button>
            </div>
        </div>
    </form>

</t:page>
