NuoDB Storefront Demo
=====================

This web application implements a mock storefront to highlight some of NuoDB's great features.  You can browse products, add items to you cart, write reviews, and checkout.  You can also simulate thousands of concurrent (simulated) shoppers with customizable workload characteristics.  

![ScreenShot](https://raw.github.com/nuodb/nuodb-samples/master/StorefrontDemo/doc/home.png)

While the store itself is not really open for business, the queries being run under the hood are quite real!

Getting Started (command line)
---------------

1. Grab the source code from Git:

        git clone git://github.com/nuodb/nuodb-samples.git

2. Create a NuoDB "Storefront" database with "StorefrontUser" as the username and "StorefrontUser" as the password.  If you want to change these defaults, edit the `nuodb-samples/StorefrontDemo/src/main/resources/hibernate.cfg.xml` file 
   or specify overrides using command line arguments described in step 3.
   
3. Run the Storefront web app:

        cd nuodb-samples\StorefrontDemo
        mvn tomcat7:run [args]
        
   The following (optional) Storefront environment settings may be provided:
   
        -Dstorefront.url=http://{host}:{port}/{context} 
        
      >	The externally-accessible URL of the Storefront.  Web browsers should be able to access the Storefront
      >	web app using this URL.  The URL is used by the Storefront front-end for communication and switching among instances.
      > You may use `{host}`, `{port}`, and `{context}` fields as placeholders for auto-detected values, 
      > or specify a completely custom URL with no placeholders.
      >    
      > If you do not specify a command line value, the default is pulled from the `public-url` context param of web.xml.
      > The default is `http://{host}:{port}/{context}`. 
                                                              
		-Dstorefront.region=Default
		
	  > The name of the region in which this Storefront instance is running.  If you are using NuoDB 2.0 or greater
	  > and don't explicity specify a region name here, the region name is auto-detected by querying the `NODES` table
	  > (`GEOREGION` column).

		-Dstorefront.db.name=name@host[:port]
		
	  > The name, hostname/IP, and port of the NuoDB instance to connect to.  If you'd like to run the Storefront with MySQL 
	  > instead, swap the `nuodb-samples/StorefrontDemo/src/main/resources/hibernate.cfg.xml` file with
	  > `nuodb-samples/StorefrontDemo/src/main/resources/hibernate-mysql.cfg.xml` and add necessary dependencies to the pom.xml file.
	  > The MySQL dependencies already exist in the pom.xml file but need to be uncommented.      
	  
		-Dstorefront.db.user=StorefrontUser
		
	  > The username of the database account to use when connecting.

		-Dstorefront.db.password=StorefrontUser
		
	  > The password of the database account to use when connecting. 


   The Maven Tomcat plugin also supports [some settings](http://tomcat.apache.org/maven-plugin-2.1/tomcat7-maven-plugin/run-mojo.html), including:
   
		-Dmaven.tomcat.port=8888
		
	  > The port on which the Storefront web app should run.
                                                               
   
4. Explore the web app at `http://localhost:8888/StorefrontDemo` (or whichever port you've chosen).

Getting Started (Eclipse)
---------------

See the [Storefront Demo Developer Setup Guide](NuoDB-Storefront.ppt) for step-by-step instructions with screenshots.

StorefrontApp command line utility
-----------------------------------

`com.nuodb.storefront.StorefrontApp` supports the following actions via command line arguments.  

- `create` -- create schema
- `drop` -- drop schema
- `showddl` -- display drop and create DDL
- `generate` -- generate dummy storefront data
- `load` -- load storefront data from src/main/resources/sample-products.json file
- `simulate` -- simulate customer activity with a mix of workloads for 100 seconds
- `benchmark` -- run benchmark simulation for 1 minute


If you specify multiple actions, they are executed in sequence.  For example, to recreate the schema,  initialize it with about 1,000 products, and then stress test the app with simulated load for 1 minute, specify the command line "drop create load simulate".


Web app
-------
The storefront web app features 3 pages:

1. Product listing page (as shown above)
2. Product details page
   ![ScreenShot](https://raw.github.com/nuodb/nuodb-samples/master/StorefrontDemo/doc/product.png)
3. Product review form
   ![ScreenShot](https://raw.github.com/nuodb/nuodb-samples/master/StorefrontDemo/doc/review.png)
4. Cart contents and checkout page
   ![ScreenShot](https://raw.github.com/nuodb/nuodb-samples/master/StorefrontDemo/doc/cart.png)

Admin interface
---------------

You can use the admin interface to view database, service, storefront, and simulator statistics, and access a "control panel" to adjust how many users of each simulated workload are active.

The interface is accessible by browing to the web app and going to the "/admin" subdirectory.

   ![ScreenShot](https://raw.github.com/nuodb/nuodb-samples/master/StorefrontDemo/doc/admin-simulator.png)

   ![ScreenShot](https://raw.github.com/nuodb/nuodb-samples/master/StorefrontDemo/doc/admin-service.png)
   
   ![ScreenShot](https://raw.github.com/nuodb/nuodb-samples/master/StorefrontDemo/doc/admin-store.png)

Key libraries used by this project
----------------------------------
Server side librares:
- **Jersey** -- JSON-based RESTful API
- **Hibernate** -- ORM mapping
- **NuoDB JDBC driver, Hibernate dialect, and DataSource connection pool**
- **GoogleCode Generic DAO** -- thin data access wrapper on Hibernate for searching, saving, etc.

Client-side libraries:
- **Twitter Bootstrap** -- look & feel
- **Handlebars** -- HTML templating
- **jQuery**
- **jQuery RateIt plug-in** -- star ratings

Admin client-side libraries:
- **Sencha Ext JS** -- look & feel
- **jQuery Sparkline plug-in** -- sparklines in the header
