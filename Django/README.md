# Django Testing (NuoDB vs. Postgresql)

These directions are assuming you are running the tests on a centos system. The directions for building the sample app used in these tests can be found on [Django's website](https://docs.djangoproject.com/en/dev/intro/tutorial01/).

### Installation

To perform these tests you will need the following:

* apache
* mod_wsgi
* postgresql
* psycopg

To get these perform the following commands…

```
yum update
yum install python-setuptools httpd mod_wsgi postgresql python-psycopg2 postgresql-server
```

[Django](https://github.com/django/django), [NuoDB](http://www.nuodb.com/download), [pynuodb](https://github.com/nuodb/nuodb-python), and [django_pynuodb](https://github.com/nuodb/nuodb-django) need to be installed as well.


### Setting up the databases

####NuoDB:

Start the broker:  

```
/opt/nuodb/bin/run-nuoagent.sh
```

Start the TE and SM:

```
java -jar /opt/nuodb/jar/nuodbmanager.jar --broker localhost --password bird
nuodb [domain] > start process sm host localhost database Thousand archive /tmp/nuodb/data initialize true
nuodb [domain/Thousand] > start process te host localhost database Thousand options '--dba-user dba --dba-password goalie'
```

####Postgresql

Initialize our database directory:

```
initdb -D /tmp/pgsql/data
```

Start postgres:

```
postgres -D /tmp/pgsql/data > /tmp/pgsql/logfile 2>&1 &
```

Create the database:

```
createdb Thousand
```


### Populating the databases

For this we need our Django app to be in a common directory

```
sudo cp -r mysite /var/www/
```

In the mysite/mysite/settings.py file there are databases dictionary configurations for four different database setups, in this example we will only be using two of these. First we will setup our NuoDB database, to do this uncomment the following lines in settings.py

```
# DATABASES = {
#     'default': {
#         'ENGINE': 'django_pynuodb',
#         'NAME': 'Thousand',
#         'DBA_USER': 'dba',
#         'DBA_PASSWORD': 'goalie',
#         'HOST': 'localhost',
#         'PORT': '48004',
#     }
# }
```

Now we are ready to set up our database with syncdb (NOTE: Before doing this make sure that your PYTHONPATH is properly setup to point at the django_pynuodb and pynuodb libraries)

```
python /var/www/mysite/manage.py syncdb
```

The last step is to run the [python file](https://github.com/nuodb/nuodb-samples/tree/master/Django/popscript) to populate our NuoDB database.

```
python nuodb_poll.py
```

In order to populate the postgres database simply comment out the databases dictionary mentioned above, and uncomment the following dictionary instead

```
# DATABASES = {
#     'default': {
#         'ENGINE': 'django.db.backends.postgresql_psycopg2',
#         'NAME': 'Thousand',
#         'USER': 'build',
#         'PASSWORD': '',
#         'HOST': 'localhost',
#         'PORT': '',
#     }
# }
```

Then all you need to do is perform another syncdb and then run the postgres_poll.py script to populate the database.

### Setting up apache

To do this we need to first configure apache so that it will know to utilize our django application. In the configure file /etc/httpd/conf/httpd.conf add the following lines

```
LoadModule wsgi_module modules/mod_wsgi.so

Alias /static/ /var/www/mysite/static/

WSGIPythonPath /var/www/mysite:/usr/lib/python2.6/site-packages:/usr/lib64/python2.6/site-packages:/opt/python2.7.3/lib/python2.7/site-packages

<Directory /var/www/mysite/static>
    Order deny,allow
    Allow from all
</Directory>

WSGIScriptAlias / /var/www/mysite/mysite/wsgi.py

<Directory /var/www/mysite/mysite>
    <Files wsgi.py>
        Order deny,allow
        Allow from all
    </Files>
</Directory>
```

Of course note that WSGIPythonPath is where you set the PYTHONPATH to point to your library files, so you may need to modify this line.

From here we need to collect the static files for our application

```
python /var/www/mysite/manage.py collectstatic
```

Now we are ready to run our application through apache

```
service httpd start
```

### Testing through apache

Finally we are ready to test, for this we will be using apache benchmark (this test is performing 1000 connections with 10 concurrently)

```
ab -n 1000 -c 10 localhost/polls
```

Of course this test is connecting from the same machine, but feel free to do this over a network as well.

