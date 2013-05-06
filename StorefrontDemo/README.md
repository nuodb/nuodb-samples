NuoDB Storefront Demo
=====================

This web application implements a mock storefront to highlight some of NuoDB's great features.  You can browse products, add items to you cart, write reviews, and checkout.  You can also simulate thousands of concurrent (simulated) shoppers with customizable workload characteristics.  

![ScreenShot](https://raw.github.com/nuodb/nuodb-samples/master/StorefrontDemo/doc/home.png)

While the store itself is not really open for business, the queries being run under the hood are quite real!

Getting Started (command line)
---------------

1.  Grab the source code from Git:

        git clone git://github.com/nuodb/nuodb-samples.git

2.  Create a NuoDB "Storefront" database with "StorefrontUser" as the username and "StorefrontUser" as the password.  If you want to change these defaults, edit the `nuodb-samples/StorefrontDemo/src/main/resources/hibernate.cfg.xml` file.
3. Run the Storefront web app:

        cd nuodb-samples\StorefrontDemo
        mvn tomcat:run
4.  Explore the web app at `http://localhost:8888/StorefrontDemo`.  (If you want to run it using a different port, edit the `nuodb-samples/StorefrontDemo/pom.xml` file.)
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

   ![ScreenShot](https://raw.github.com/nuodb/nuodb-samples/master/StorefrontDemo/doc/admin-store.png)

   ![ScreenShot](https://raw.github.com/nuodb/nuodb-samples/master/StorefrontDemo/doc/admin-simulator.png)

Key libraries used by this project
----------------------------------
Server side librares:
- **Jersey** -- JSON-based RESTful API
- **Hibernate** -- ORM mapping
- **NuoDB Hiberante dialect**
- **GoogleCode Generic DAO** -- thin data access wrapper on Hibernate for searching, saving, etc.

Client-side libraries:
- **Twitter Bootstrap** -- look & feel
- **Handlebars** -- HTML templating
- **jQuery**
- **jQuery RateIt plug-in** -- star ratings

Admin client-side libraries:
- **Sencha Ext JS** -- look & feel
- **jQuery Sparkline plug-in** -- sparklines in the header
