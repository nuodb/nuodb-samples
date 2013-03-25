NuoDB Storefront Demo
=====================

This web application implements a mock storefront to highlight some of NuoDB's great features.  You can browse products, add items to you cart, write reviews, and checkout.  You can also simulate thousands of concurrent (simulated) shoppers with customizable workload characteristics.  While the store itself is not really open for business, the queries being run under the hood are quite real!

![ScreenShot](https://raw.github.com/nuodb/nuodb-samples/master/StorefrontDemo/doc/home.png)

Getting started
---------------

1. Build the project using Maven within the Eclipse IDE.
2. Create a NuoDB database and schema for the Storefront data.
3. Edit src/main/resources/hiberante.cfg.xml to specify your database's URL, uername, password, and schema name.
4. Seed the database with data using the runnable StorefrontApp.
5. Run the application by either:
   - Using an application server such as Tomcat and/or
   - Using the load simulator action from StorefrontApp

StorefrontApp command line utility
-----------------------------------

`com.nuodb.storefront.StorefrontApp` supports the following actions via command line arguments.  If you specify multiple actions, they are executed in sequence.

- `create` -- create schema
- `drop` -- drop schema
- `showddl` -- display drop and create DDL
- `generate` -- generate dummy storefront data
- `load` -- load storefront data from src/main/resources/sample-products.json file
- `simulate` -- simulate customer activity

For example, to recreate the schema,  initialize it with about 1,000 products, and then stress test the app with simulated load for 1 minute, specify the command line "drop create load simulate".

You can edit the workload definitions, mixes, and timings.  If you use default values, a simulation run will perodically show you output like this:

    Workload                    Active   Failed   Killed Complete |   Steps   Avg (s)    Work   Avg (s)
    SIMULATED_USER_FACTORY           0        0        0        2 |      40     0.000       2     0.000
    SIMILATED_BROWSER               20        0        0        0 |      91     4.543      10     2.337
    SIMILATED_SHOPPER_FAST          20        0        0        0 |     238     3.167       4     9.995
    
    Step:                           # Completions:
    BROWSE                                      45
    BROWSE_NEXT_PAGE                            61
    BROWSE_SEARCH                               40
    BROWSE_CATEGORY                             38
    PRODUCT_VIEW_DETAILS                        71
    PRODUCT_ADD_TO_CART                         53
    PRODUCT_ADD_REVIEW                           0
    CART_VIEW                                   10
    CART_UPDATE                                  7
    CART_CHECKOUT                                4


Storefront web pages
--------------------
The storefront features 3 pages:

1. Product listing page (as shown above)
2. Product details page
   ![ScreenShot](https://raw.github.com/nuodb/nuodb-samples/master/StorefrontDemo/doc/product.png)
3. Product review form
   ![ScreenShot](https://raw.github.com/nuodb/nuodb-samples/master/StorefrontDemo/doc/review.png)
4. Cart contents and checkout page
   ![ScreenShot](https://raw.github.com/nuodb/nuodb-samples/master/StorefrontDemo/doc/cart.png)

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
- **RateIt jQuery plug-in** -- star ratings
