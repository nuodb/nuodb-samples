<%-- Copyright (c) 2013 NuoDB, Inc. --%>
<%@page contentType="text/html" pageEncoding="UTF-8"%>
<%@taglib prefix="t" tagdir="/WEB-INF/tags"%>

<t:page showHeader="false">
    <div id="welcome">
        <!-- Top nav -->
        <div id="top-bar" class="navbar">
            <div class="navbar-inner">
                <div class="brand navbar-text">
                    <label>Welcome to the NuoDB Storefront Demo!</label>
                </div>
                <ul class="nav pull-right">
                    <t:admin-link />
                    <li><a href="https://github.com/nuodb/nuodb-samples/tree/master/StorefrontDemo" target="_blank" title="Opens the source code in a new tab (hosted by GitHub)"><i class="icon-github"></i> README</a></li>
                    <li id="lnk-show-ddl"><a href="#" title="Displays the drop and create DDL statements of the Storefront schema"><i class="icon-align-justify"></i> DDL</a></li>
                    <li><a href="http://www.nuodb.com/groups/dev-center/" target="_blank" title="Goes to the NuoDB Dev Center for technical details about this sample and NuoDB"><i class="icon-nuodb"></i> DevCenter</a></li>
                </ul>
            </div>
        </div>

        <!-- DDL -->
        <div class="row" id="ddl">
            <div class="span12">
                <h2>Storefront DDL</h2>
                <textarea></textarea>
            </div>
        </div>
        <script id="tpl-ddl" type="text/template">{{result}}</script>

        <!-- Carousel -->
        <div id="carousel" class="carousel slide">
            <ol class="carousel-indicators">
                <li data-target="#carousel" data-slide-to="0" class="active"></li>
                <li data-target="#carousel" data-slide-to="1" class=""></li>
                <li data-target="#carousel" data-slide-to="2" class=""></li>
                <li data-target="#carousel" data-slide-to="3" class=""></li>
            </ol>
            <div class="carousel-inner">
                <div class="item active">
                    <img src="img/carousel/cart.png" height="347" />
                    <div class="carousel-caption">
                        <h2>
                            Go on...<br />
                            <b>kick the tires</b>
                        </h2>
                        <h4>
                            Dive in to this <b>practical example</b> of a<br />Java web application &ndash; showcasing the<br />key benefits of NuoDB.
                        </h4>
                    </div>
                </div>
                <div class="item">
                    <img src="img/carousel/flock.png" height="347" />
                    <div class="carousel-caption">
                        <h3>
                            &nbsp;<br />Elasticity
                        </h3>
                        <h4>
                            <b>Open the floodgates</b>
                        </h4>
                        <p>
                            See how NuoDB reacts to a sudden flock of customers.<br />Use the <b>Simulated User Setup</b> and <b>Node Setup</b> forms below<br />to increase the number of customer workloads and NuoDB nodes.
                        </p>
                    </div>
                </div>
                <div class="item">
                    <img src="img/carousel/bags.png" height="347" />
                    <div class="carousel-caption">
                        <h3>
                            Multi-<br />Tenancy
                        </h3>
                        <h4>
                            <b>Hang some new shingles</b>
                        </h4>
                        <p>
                            Launch multiple stores with the same NuoDB nodes.<br />Click <b>Show DDL</b> and you&rsquo;ll have the scripts<br />to create your additional databases.
                        </p>
                    </div>
                </div>
                <div class="item">
                    <img src="img/carousel/switch.png" height="347" />
                    <div class="carousel-caption">
                        <h3>
                            Variable<br />Workloads
                        </h3>
                        <h4>
                            <b>Try a bait &amp; switch</b>
                        </h4>
                        <p>
                            Pull a 180 by changing the mix of OLTP and analytic transactions.<br />Use the <b>Simulated Users</b> form below to change<br />the ratio of back office to shopper workloads.
                        </p>
                    </div>
                </div>
            </div>
            <a class="left carousel-control" href="#carousel" data-slide="prev">‹</a> <a class="right carousel-control" href="#carousel" data-slide="next">›</a>
        </div>

        <!-- Workload controls -->
        <div class="row">
            <div class="span12">
                <h2 id="workload-sec">Simulated User Setup</h2>
                <t:messages />
                <form method="post" action="#workload-sec">
                    <table class="table table-hover table-bordered table-condensed">
                        <thead>
                            <tr>
                                <th>Workload</th>
                                <th class="text-right"><a data-toggle="tooltip" title="How long, on average, the simulated user waits between taking successive steps">Avg. think time</a></th>
                                <th class="text-right"><a data-toggle="tooltip" title="The standard deviation of the user&rsquo;s think time">Std. dev.</a></th>
                                <th class="workload-detail">Simulated Steps</th>
                                <th class="text-center"># Users</th>
                            </tr>
                        </thead>
                        <tbody id="workloads">
                        </tbody>
                    </table>
                    <p class="text-right">
                        <button class="btn btn-primary" type="submit">Update Users</button>
                        &nbsp;
                        <button class="btn" id="btn-reset" title="Sets the number of user to 0 across all workloads">Stop All</button>
                    </p>
                </form>
            </div>
        </div>
        <script id="tpl-workloads" type="text/template">    
            {{#result}}
            <tr>
                <td>
                    <div class="media">
                        <img class="pull-left" src="img/workload.png" width="32" height="32" />
                        <div class="media-body">
                            <h4 class="media-heading">{{workload.name}}</h4>
                            <div>
                                {{#if workload.autoRepeat}}
									<p class="desc workload-detail">
										<span class="label"><i class="icon-retweet icon-white"></i> Auto-repeating</span>
									</p>
								{{/if}}
                            </div>
                        </div>
                    </div>
                </td>
                <td class="workload-detail">
					<div class="steps">
                    	<ol>
                        	{{#workload.steps}}
                        		<li>{{this}}</li>
                        	{{/workload.steps}}
                    	</ol>
					</div>
                </td>
				<td class="text-right">{{{msFormat workload.avgThinkTimeMs}}</td>
				<td class="text-right">{{sqrtMsFormat workload.thinkTimeVariance}}</td>
                <td class="text-center">
					{{#if workload.avgThinkTimeMs}}
						<input class="input-mini" type="number" name="workload-{{workload.name}}" value="{{numberOrZero activeWorkerLimit}}" min="0" max="1000" step="1" />
					{{else}}
						<input readonly="readonly" title="Workloads with 0 are for benchmark running only and cannot be modified here.  This helps keep the Storefront responsive." class="input-mini" type="number" name="workload-{{workload.name}}" value="{{numberOrZero activeWorkerLimit}}" min="0" max="1000" step="1" />
					{{/if}}
				</td>
            </tr>
            {{/result}}  
        </script>
        <hr />
    </div>

    <div class="row">
        <div class="span12">
            <h2 id="workload-sec">Node Setup</h2>
            <p>To add nodes to your NuoDB cluster, use the NuoDB Console.</p>
            <p>If you are running NuoDB locally with default settings, you will find the Console at <a href="http://localhost:8080/" target="_blank">http://localhost:8080/</a>.</p>
        </div>
    </div>
    
    <div class="footer">Copyright &copy; 2013 NuoDB, Inc. All rights reserved.</div>
</t:page>
