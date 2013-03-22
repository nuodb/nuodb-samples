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
                                <a href="product?productId={{id}}" class="thumbnail">
                                    <img data-src="holder.js/260x180" alt="260x180" style="width: 260px; height: 180px;" src="data:image/png;base64,iVBORw0KGgoAAAANSUhEUgAAAQQAAAC0CAYAAABytVLLAAAIhUlEQVR4Xu3avYsUWxMH4N7AL9DARM1EDNVQBP99IxMxUoxFMJBFEBHBj/ftgbO07cxsrXW99K16TMT1TM2pp7p/0907J6enpz8nfwgQIPB/gROB4DggQGAICATHAgECZwICwcFAgIBAcAwQIPC7gCsERwUBAq4QHAMECLhCcAwQIHBEwC2Dw4MAAbcMjgECBNwyOAYIEHDL4BggQCAi4BlCRMkaAk0EBEKTQWuTQERAIESUrCHQREAgNBm0NglEBARCRMkaAk0EBEKTQWuTQERAIESUrCHQREAgNBm0NglEBARCRMkaAk0EBEKTQWuTQERAIESUrCHQREAgNBm0NglEBARCRMkaAk0EBEKTQWuTQERAIESUrCHQREAgNBm0NglEBARCRMkaAk0EBEKTQWuTQERAIESUrCHQREAgNBm0NglEBARCRMkaAk0EBEKTQWuTQERAIESUrCHQREAgNBm0NglEBARCRMkaAk0EBEKTQWuTQERAIESUrCHQREAgNBm0NglEBARCRMkaAk0EBEKTQWuTQERAIESUrCHQREAgNBm0NglEBARCRMkaAk0EBEKTQWuTQERAIESUrCHQREAgNBm0NglEBARCRMkaAk0EBEKTQWuTQERAIESUrCHQREAgNBm0NglEBARCRMkaAk0EBEKTQWuTQERAIESUrCHQREAgNBm0NglEBARCRMkaAk0EBEKTQWuTQERAIESUrCHQREAgNBm0NglEBARCRMkaAk0EBEKTQWuTQERAIESUrCHQREAgNBm0NglEBARCRMkaAk0EBEKTQWuTQERAIESUrCHQREAgNBm0NglEBARCRMkaAk0EBEKTQWuTQERAIESUrCHQREAgNBm0NglEBARCRMkaAk0EBEKTQWuTQERAIESUrCHQREAgNBm0NglEBARCRMkaAk0EBEKTQWuTQERAIESUrCHQREAgNBm0NglEBARCRMkaAk0EBEKTQWuTQERAIESUrCHQREAgbHDQr1+/nt69e3e2sxs3bkxPnjz5Zaenp6fTy5cvpx8/fux+vm/N27dvpzdv3py97t69e9P9+/f/uOMXL15M8/vevn17evTo0S91xv+NH+5b80/v548b8cKDAgJhYwfH+sQa21ue8Osw2HcSrk++seZPQ2FZb32yrwPs39jPxsZWZjsCYUOj/Pbt2/Ts2bPp+/fv0zhxlyfiw4cPp1u3bk3Pnz+fPn/+/Nuak5OT6enTp9OVK1fO1oyTd5y0V69e3V1tXLp0Kdz5sU//5Z7X7/W39hPeuIUXFhAIFyb7ey8YJ/84ka5duzatQ+Lu3bu70JhvFeaTf16z/rO8gphD5M6dO7tL/fkW4+fPn7vXffny5eyWY4TP+nXL8JnfYw6ar1+//nLLsNxf5L0OrdnXx9+TVvmQgEDY+LGxPkkvX768O5Hnv+dP+48fP+46WF7Gr0/+dbCMk3J88s91Hj9+PM3/nq88Rq05dOarkevXr08PHjz47apjft+xZvm69dXIp0+ffgmjQ/vZ+ChabE8gbHjMy5NtPEM49GxgbmO95tiVxvxwcfnpPl9FvH//flq+Zkmz78Qf/7+sM362rBO58sk87NzwCP9zWxMIGx3Z8gTcd3LN214/ZxjrPnz4sPvtwnmBMNeIPvk/FgjnPcAUCBs9yPZsSyBscFaHwmDeauR2YNxWjOcFxy7Rl5/uh64ODt0azD8/9iB01BvPKyL72eA4Wm1JIGxs3MfC4KKBMNc67yHe+leG+74/cCwQIp/+N2/ePHuAed5+NjaOdtsRCBsb+XjQd5F7+fVJGf214/JqY35o+OrVq91DwnHSRp4hLB96Rn5V+k/8GnRjIyu1HYGwoXEe+sLR2OK+E265/eWn+3n39ZHfDiy/q3DoGcLy52vK5ZepztvPhsbQeisCYUPjP/SNv3UgLG8dxleXL/pV4fFehx48rusde6g472f95aV/46vUGxpdma0IhDKj1AiBvIBAyBuqQKCMgEAoM0qNEMgLCIS8oQoEyggIhDKj1AiBvIBAyBuqQKCMgEAoM0qNEMgLCIS8oQoEyggIhDKj1AiBvIBAyBuqQKCMgEAoM0qNEMgLCIS8oQoEyggIhDKj1AiBvIBAyBuqQKCMgEAoM0qNEMgLCIS8oQoEyggIhDKj1AiBvIBAyBuqQKCMgEAoM0qNEMgLCIS8oQoEyggIhDKj1AiBvIBAyBuqQKCMgEAoM0qNEMgLCIS8oQoEyggIhDKj1AiBvIBAyBuqQKCMgEAoM0qNEMgLCIS8oQoEyggIhDKj1AiBvIBAyBuqQKCMgEAoM0qNEMgLCIS8oQoEyggIhDKj1AiBvIBAyBuqQKCMgEAoM0qNEMgLCIS8oQoEyggIhDKj1AiBvIBAyBuqQKCMgEAoM0qNEMgLCIS8oQoEyggIhDKj1AiBvIBAyBuqQKCMgEAoM0qNEMgLCIS8oQoEyggIhDKj1AiBvIBAyBuqQKCMgEAoM0qNEMgLCIS8oQoEyggIhDKj1AiBvIBAyBuqQKCMgEAoM0qNEMgLCIS8oQoEyggIhDKj1AiBvIBAyBuqQKCMgEAoM0qNEMgLCIS8oQoEyggIhDKj1AiBvIBAyBuqQKCMgEAoM0qNEMgLCIS8oQoEyggIhDKj1AiBvIBAyBuqQKCMgEAoM0qNEMgLCIS8oQoEyggIhDKj1AiBvIBAyBuqQKCMgEAoM0qNEMgLCIS8oQoEyggIhDKj1AiBvIBAyBuqQKCMgEAoM0qNEMgLCIS8oQoEyggIhDKj1AiBvIBAyBuqQKCMgEAoM0qNEMgLCIS8oQoEyggIhDKj1AiBvIBAyBuqQKCMgEAoM0qNEMgLCIS8oQoEyggIhDKj1AiBvIBAyBuqQKCMgEAoM0qNEMgLCIS8oQoEyggIhDKj1AiBvMD/ANbXEGO2tfHZAAAAAElFTkSuQmCC" />
                                    <div class="prod-metadata">
                                        <div class="name">{{name}}</div>
                                        <div class="desc">{{description}}</div>
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
