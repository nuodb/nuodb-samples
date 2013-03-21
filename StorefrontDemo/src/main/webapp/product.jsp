<%@page contentType="text/html" pageEncoding="UTF-8"%>
<%@taglib prefix="t" tagdir="/WEB-INF/tags"%>

<t:page>
    <section id="product"></section>
    <script id="tpl-product" type="text/template">
        <!-- Product info  -->
        <div id="product-info" class="row">
            <div class="span3">
                <img class="img-polaroid img-rounded" style="width: 260px; height: 180px;"
                    src="data:image/png;base64,iVBORw0KGgoAAAANSUhEUgAAAQQAAAC0CAYAAABytVLLAAAIhUlEQVR4Xu3avYsUWxMH4N7AL9DARM1EDNVQBP99IxMxUoxFMJBFEBHBj/ftgbO07cxsrXW99K16TMT1TM2pp7p/0907J6enpz8nfwgQIPB/gROB4DggQGAICATHAgECZwICwcFAgIBAcAwQIPC7gCsERwUBAq4QHAMECLhCcAwQIHBEwC2Dw4MAAbcMjgECBNwyOAYIEHDL4BggQCAi4BlCRMkaAk0EBEKTQWuTQERAIESUrCHQREAgNBm0NglEBARCRMkaAk0EBEKTQWuTQERAIESUrCHQREAgNBm0NglEBARCRMkaAk0EBEKTQWuTQERAIESUrCHQREAgNBm0NglEBARCRMkaAk0EBEKTQWuTQERAIESUrCHQREAgNBm0NglEBARCRMkaAk0EBEKTQWuTQERAIESUrCHQREAgNBm0NglEBARCRMkaAk0EBEKTQWuTQERAIESUrCHQREAgNBm0NglEBARCRMkaAk0EBEKTQWuTQERAIESUrCHQREAgNBm0NglEBARCRMkaAk0EBEKTQWuTQERAIESUrCHQREAgNBm0NglEBARCRMkaAk0EBEKTQWuTQERAIESUrCHQREAgNBm0NglEBARCRMkaAk0EBEKTQWuTQERAIESUrCHQREAgNBm0NglEBARCRMkaAk0EBEKTQWuTQERAIESUrCHQREAgNBm0NglEBARCRMkaAk0EBEKTQWuTQERAIESUrCHQREAgNBm0NglEBARCRMkaAk0EBEKTQWuTQERAIESUrCHQREAgNBm0NglEBARCRMkaAk0EBEKTQWuTQERAIESUrCHQREAgNBm0NglEBARCRMkaAk0EBEKTQWuTQERAIESUrCHQREAgNBm0NglEBARCRMkaAk0EBEKTQWuTQERAIESUrCHQREAgNBm0NglEBARCRMkaAk0EBEKTQWuTQERAIESUrCHQREAgNBm0NglEBARCRMkaAk0EBEKTQWuTQERAIESUrCHQREAgbHDQr1+/nt69e3e2sxs3bkxPnjz5Zaenp6fTy5cvpx8/fux+vm/N27dvpzdv3py97t69e9P9+/f/uOMXL15M8/vevn17evTo0S91xv+NH+5b80/v548b8cKDAgJhYwfH+sQa21ue8Osw2HcSrk++seZPQ2FZb32yrwPs39jPxsZWZjsCYUOj/Pbt2/Ts2bPp+/fv0zhxlyfiw4cPp1u3bk3Pnz+fPn/+/Nuak5OT6enTp9OVK1fO1oyTd5y0V69e3V1tXLp0Kdz5sU//5Z7X7/W39hPeuIUXFhAIFyb7ey8YJ/84ka5duzatQ+Lu3bu70JhvFeaTf16z/rO8gphD5M6dO7tL/fkW4+fPn7vXffny5eyWY4TP+nXL8JnfYw6ar1+//nLLsNxf5L0OrdnXx9+TVvmQgEDY+LGxPkkvX768O5Hnv+dP+48fP+46WF7Gr0/+dbCMk3J88s91Hj9+PM3/nq88Rq05dOarkevXr08PHjz47apjft+xZvm69dXIp0+ffgmjQ/vZ+ChabE8gbHjMy5NtPEM49GxgbmO95tiVxvxwcfnpPl9FvH//flq+Zkmz78Qf/7+sM362rBO58sk87NzwCP9zWxMIGx3Z8gTcd3LN214/ZxjrPnz4sPvtwnmBMNeIPvk/FgjnPcAUCBs9yPZsSyBscFaHwmDeauR2YNxWjOcFxy7Rl5/uh64ODt0azD8/9iB01BvPKyL72eA4Wm1JIGxs3MfC4KKBMNc67yHe+leG+74/cCwQIp/+N2/ePHuAed5+NjaOdtsRCBsb+XjQd5F7+fVJGf214/JqY35o+OrVq91DwnHSRp4hLB96Rn5V+k/8GnRjIyu1HYGwoXEe+sLR2OK+E265/eWn+3n39ZHfDiy/q3DoGcLy52vK5ZepztvPhsbQeisCYUPjP/SNv3UgLG8dxleXL/pV4fFehx48rusde6g472f95aV/46vUGxpdma0IhDKj1AiBvIBAyBuqQKCMgEAoM0qNEMgLCIS8oQoEyggIhDKj1AiBvIBAyBuqQKCMgEAoM0qNEMgLCIS8oQoEyggIhDKj1AiBvIBAyBuqQKCMgEAoM0qNEMgLCIS8oQoEyggIhDKj1AiBvIBAyBuqQKCMgEAoM0qNEMgLCIS8oQoEyggIhDKj1AiBvIBAyBuqQKCMgEAoM0qNEMgLCIS8oQoEyggIhDKj1AiBvIBAyBuqQKCMgEAoM0qNEMgLCIS8oQoEyggIhDKj1AiBvIBAyBuqQKCMgEAoM0qNEMgLCIS8oQoEyggIhDKj1AiBvIBAyBuqQKCMgEAoM0qNEMgLCIS8oQoEyggIhDKj1AiBvIBAyBuqQKCMgEAoM0qNEMgLCIS8oQoEyggIhDKj1AiBvIBAyBuqQKCMgEAoM0qNEMgLCIS8oQoEyggIhDKj1AiBvIBAyBuqQKCMgEAoM0qNEMgLCIS8oQoEyggIhDKj1AiBvIBAyBuqQKCMgEAoM0qNEMgLCIS8oQoEyggIhDKj1AiBvIBAyBuqQKCMgEAoM0qNEMgLCIS8oQoEyggIhDKj1AiBvIBAyBuqQKCMgEAoM0qNEMgLCIS8oQoEyggIhDKj1AiBvIBAyBuqQKCMgEAoM0qNEMgLCIS8oQoEyggIhDKj1AiBvIBAyBuqQKCMgEAoM0qNEMgLCIS8oQoEyggIhDKj1AiBvIBAyBuqQKCMgEAoM0qNEMgLCIS8oQoEyggIhDKj1AiBvIBAyBuqQKCMgEAoM0qNEMgLCIS8oQoEyggIhDKj1AiBvIBAyBuqQKCMgEAoM0qNEMgLCIS8oQoEyggIhDKj1AiBvIBAyBuqQKCMgEAoM0qNEMgLCIS8oQoEyggIhDKj1AiBvIBAyBuqQKCMgEAoM0qNEMgLCIS8oQoEyggIhDKj1AiBvMD/ANbXEGO2tfHZAAAAAElFTkSuQmCC" />
            </div>

            <div class="span6">
                <h2 class="name">{{name}}</h2>
                <p>{{description}}</p>
				{{#if categories}}
					<p class="categories">					
						{{#categories}}
  							<span class="label">{{this}}</span>
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

				{{#reviews}}
                <div class="media media-review">
                    <div class="pull-left">
                        <img class="media-object img-polaroid img-rounded" src="http://www.gravatar.com/avatar/205e460b479e2e5b48aec07710c08d51?s=64&d=mm" />
                    </div>
                    <div class="media-body">
                        <h4 class="media-heading">
                            <div class="rateit" data-rateit-value="{{rating}}" data-rateit-ispreset="true" data-rateit-readonly="true"></div>
                            {{title}}
                        </h4>
                        <div class="date">{{dateFormat dateAdded}}</div>
                        <div class="byline">
                            By <b>Customer {{id}}</b>
                        </div>
                        <p class="comment">{{comments}}</p>
                    </div>
                </div>
				{{/reviews}}

                <p>
                    Do you have this product? <a href="#dlg-add-review" data-toggle="modal">Submit your review</a>.
                </p>
            </div>
        </div>
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
