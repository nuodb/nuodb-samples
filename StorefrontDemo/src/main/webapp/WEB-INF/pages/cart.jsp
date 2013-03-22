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
                        	                    <a class="pull-left" href="product?productId={{product.id}}"><img class="img-rounded pull-left" style="width: 130px; height: 90px;"
                            	                    src="data:image/png;base64,iVBORw0KGgoAAAANSUhEUgAAAQQAAAC0CAYAAABytVLLAAAIhUlEQVR4Xu3avYsUWxMH4N7AL9DARM1EDNVQBP99IxMxUoxFMJBFEBHBj/ftgbO07cxsrXW99K16TMT1TM2pp7p/0907J6enpz8nfwgQIPB/gROB4DggQGAICATHAgECZwICwcFAgIBAcAwQIPC7gCsERwUBAq4QHAMECLhCcAwQIHBEwC2Dw4MAAbcMjgECBNwyOAYIEHDL4BggQCAi4BlCRMkaAk0EBEKTQWuTQERAIESUrCHQREAgNBm0NglEBARCRMkaAk0EBEKTQWuTQERAIESUrCHQREAgNBm0NglEBARCRMkaAk0EBEKTQWuTQERAIESUrCHQREAgNBm0NglEBARCRMkaAk0EBEKTQWuTQERAIESUrCHQREAgNBm0NglEBARCRMkaAk0EBEKTQWuTQERAIESUrCHQREAgNBm0NglEBARCRMkaAk0EBEKTQWuTQERAIESUrCHQREAgNBm0NglEBARCRMkaAk0EBEKTQWuTQERAIESUrCHQREAgNBm0NglEBARCRMkaAk0EBEKTQWuTQERAIESUrCHQREAgNBm0NglEBARCRMkaAk0EBEKTQWuTQERAIESUrCHQREAgNBm0NglEBARCRMkaAk0EBEKTQWuTQERAIESUrCHQREAgNBm0NglEBARCRMkaAk0EBEKTQWuTQERAIESUrCHQREAgNBm0NglEBARCRMkaAk0EBEKTQWuTQERAIESUrCHQREAgNBm0NglEBARCRMkaAk0EBEKTQWuTQERAIESUrCHQREAgNBm0NglEBARCRMkaAk0EBEKTQWuTQERAIESUrCHQREAgNBm0NglEBARCRMkaAk0EBEKTQWuTQERAIESUrCHQREAgNBm0NglEBARCRMkaAk0EBEKTQWuTQERAIESUrCHQREAgNBm0NglEBARCRMkaAk0EBEKTQWuTQERAIESUrCHQREAgNBm0NglEBARCRMkaAk0EBEKTQWuTQERAIESUrCHQREAgbHDQr1+/nt69e3e2sxs3bkxPnjz5Zaenp6fTy5cvpx8/fux+vm/N27dvpzdv3py97t69e9P9+/f/uOMXL15M8/vevn17evTo0S91xv+NH+5b80/v548b8cKDAgJhYwfH+sQa21ue8Osw2HcSrk++seZPQ2FZb32yrwPs39jPxsZWZjsCYUOj/Pbt2/Ts2bPp+/fv0zhxlyfiw4cPp1u3bk3Pnz+fPn/+/Nuak5OT6enTp9OVK1fO1oyTd5y0V69e3V1tXLp0Kdz5sU//5Z7X7/W39hPeuIUXFhAIFyb7ey8YJ/84ka5duzatQ+Lu3bu70JhvFeaTf16z/rO8gphD5M6dO7tL/fkW4+fPn7vXffny5eyWY4TP+nXL8JnfYw6ar1+//nLLsNxf5L0OrdnXx9+TVvmQgEDY+LGxPkkvX768O5Hnv+dP+48fP+46WF7Gr0/+dbCMk3J88s91Hj9+PM3/nq88Rq05dOarkevXr08PHjz47apjft+xZvm69dXIp0+ffgmjQ/vZ+ChabE8gbHjMy5NtPEM49GxgbmO95tiVxvxwcfnpPl9FvH//flq+Zkmz78Qf/7+sM362rBO58sk87NzwCP9zWxMIGx3Z8gTcd3LN214/ZxjrPnz4sPvtwnmBMNeIPvk/FgjnPcAUCBs9yPZsSyBscFaHwmDeauR2YNxWjOcFxy7Rl5/uh64ODt0azD8/9iB01BvPKyL72eA4Wm1JIGxs3MfC4KKBMNc67yHe+leG+74/cCwQIp/+N2/ePHuAed5+NjaOdtsRCBsb+XjQd5F7+fVJGf214/JqY35o+OrVq91DwnHSRp4hLB96Rn5V+k/8GnRjIyu1HYGwoXEe+sLR2OK+E265/eWn+3n39ZHfDiy/q3DoGcLy52vK5ZepztvPhsbQeisCYUPjP/SNv3UgLG8dxleXL/pV4fFehx48rusde6g472f95aV/46vUGxpdma0IhDKj1AiBvIBAyBuqQKCMgEAoM0qNEMgLCIS8oQoEyggIhDKj1AiBvIBAyBuqQKCMgEAoM0qNEMgLCIS8oQoEyggIhDKj1AiBvIBAyBuqQKCMgEAoM0qNEMgLCIS8oQoEyggIhDKj1AiBvIBAyBuqQKCMgEAoM0qNEMgLCIS8oQoEyggIhDKj1AiBvIBAyBuqQKCMgEAoM0qNEMgLCIS8oQoEyggIhDKj1AiBvIBAyBuqQKCMgEAoM0qNEMgLCIS8oQoEyggIhDKj1AiBvIBAyBuqQKCMgEAoM0qNEMgLCIS8oQoEyggIhDKj1AiBvIBAyBuqQKCMgEAoM0qNEMgLCIS8oQoEyggIhDKj1AiBvIBAyBuqQKCMgEAoM0qNEMgLCIS8oQoEyggIhDKj1AiBvIBAyBuqQKCMgEAoM0qNEMgLCIS8oQoEyggIhDKj1AiBvIBAyBuqQKCMgEAoM0qNEMgLCIS8oQoEyggIhDKj1AiBvIBAyBuqQKCMgEAoM0qNEMgLCIS8oQoEyggIhDKj1AiBvIBAyBuqQKCMgEAoM0qNEMgLCIS8oQoEyggIhDKj1AiBvIBAyBuqQKCMgEAoM0qNEMgLCIS8oQoEyggIhDKj1AiBvIBAyBuqQKCMgEAoM0qNEMgLCIS8oQoEyggIhDKj1AiBvIBAyBuqQKCMgEAoM0qNEMgLCIS8oQoEyggIhDKj1AiBvIBAyBuqQKCMgEAoM0qNEMgLCIS8oQoEyggIhDKj1AiBvIBAyBuqQKCMgEAoM0qNEMgLCIS8oQoEyggIhDKj1AiBvIBAyBuqQKCMgEAoM0qNEMgLCIS8oQoEyggIhDKj1AiBvIBAyBuqQKCMgEAoM0qNEMgLCIS8oQoEyggIhDKj1AiBvIBAyBuqQKCMgEAoM0qNEMgLCIS8oQoEyggIhDKj1AiBvMD/ANbXEGO2tfHZAAAAAElFTkSuQmCC" /></a>
                                	            <div class="media-body">
                                    	            <h4 class="media-heading">{{product.name}}</h4>
													<div class="prod-metadata">
														<div class="desc">{{product.description}}</div>
													</div>
    	                                        </div>
        	                                </div>    
            	                        </td>
                	                    <td class="price text-right">{{priceFormat product.unitPrice}}</td>
                    	                <td class="text-right"><input class="input-mini" type="number" name="product-{{product.id}}" value="{{quantity}}" min="0" step="1" /></td>
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
				{{else}}
        	    	<p>
            	    	<button class="btn" onclick="document.location.href='products';">Keep Shopping</button>
	            	</p>
				{{/if}}
	        </div>
		</script>
    </div>
</t:page>
