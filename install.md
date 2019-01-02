# Install instructions

## Warning, important note

there is **one special thing to do before running Semanlink for the first time**, (defining the "context" of the web application) so **please read this file**. (Tomcat users: you cannot just drop the semanlink.war file in Tomcat's "webapps" directory. Sorry for that but, when starting, Semanlink servlet needs to know some information that cannot be guessed.)

## **Prerequesites**

Semanlink is a web application. You MUST have a **servlet engine** installed on your computer in order to use it. If you don't, you can download and install Tomcat (latest stable release recommended) from <http://tomcat.apache.org/>.

## Define the "context" of the web application

Before running Semanlink, you MUST define the "**context**" of the application, which include some special parameters that you have to specify. (That's because Semanlink needs to know, before starting, where you want to store the information that you'll produce using it, and only you can decide). That's an easy process:  
The file called "**semanlink.xml**" (included at the first level of the directory of the distribution) is a template for the definition of Semanlink's context. Three things need to be modified in the file:

*   **docBase**: you MUST change the value given to docBase with the absolute path to the semanlink WAR file (just replace the dummy value 'path-to-semanlink.war' with the actual one, for instance, on windows, something such as 'C:\semanlink\semanlink.war')
*   **semanlinkURL**: you MUST define the value of parameter "semanlinkURL" with the URL where semanlink will be running. Note that semanlinkURL ends with the value of the "path" attribute ("/semanlink", unless you change it - these instructions will suppose that you do not change this "path" attribute) (I don't like to have to request to define this semanlinkURL parameter, but it it is the root uri of a lot of resources involved in triples that semanlink save, using relative uris, in datafiles that are loaded are startup - and servlet doesn't know the url where it is running during its init. Better to have this value written only once than in a lot of places)
*   **semanlinkDataDir**: you MUST also set the parameter "semanlinkDataDir" to the path to a directory where semanlink will store data. The servlet must have permission to write to this directory.

Leave other data unchanged.

### Context definition examples

All examples suppose that semanlink will be running at 127.0.0.1:8080/semanlink

#### Mac OSX example

Here in an example of context on Mac OSX. The user has created a directory "SemanlinkStuff" inside her "[short-username]" directory (replace "[short-username]" with appropriate value). She has copied in this directory the "semanlink.war" file from the unzipped distribution of semanlink (or she has created a symbolic link to it. Beware, an alias created with the Finder would'nt work). (Note that it is a good idea to avoid having the version number of the release in the path to the war file that gets stored in the context, because it makes upgrading to a newer release easier when one becomes available). Finally, she created inside the "SemanlinkStuff" directory a subdirectory called dataDir to store she data that she will be creating using Semanlink.

```

<Context path="/semanlink" docBase="/Users/[short-username]/SemanlinkStuff/semanlink.war" reloadable="true" debug="0">
	<Parameter name="semanlinkURL" value="http://127.0.0.1:8080/semanlink" override="false"/>
	<Parameter name="semanlinkDataDir" value="/Users/[short-username]/SemanlinkStuff/dataDir" override="false"/>
</Context>
```

#### Other UNIX

See Mac OSX example.

#### Windows example

Here in an example of context on Windows. The user has created on C: a directory called "SemanlinkStuff". She has copied in this directory the "semanlink.war" file from the unzipped distribution of semanlink (Note that it is a good idea to avoid having the version number of the release in the path to the war file that gets stored in the context, because it makes upgrading to a newer release easier when one become available). Finally, she created inside the "SemanlinkStuff" directory a subdirectory called dataDir to store she data that she will be creating using Semanlink.

```
<Context path="/semanlink" docBase="C:\SemanlinkStuff\semanlink.war" reloadable="true" debug="0">
	<Parameter name="semanlinkURL" value="http://127.0.0.1:8080/semanlink" override="false"/>
	<Parameter name="semanlinkDataDir" value="C:\SemanlinkStuff\dataDir" override="false"/>
</Context>
```

(In these two examples, the directory containing semanlink program has been renamed to "semanlink" without the version number, which is probably a good idea when it comes to upgrading)

## Instruct your servlet engine to start semanlink with the context you just defined

## That's it!

You're now ready to run Semanlink (instruct your servlet engine to do so. You may have to restart it. With Tomcat, you should be able to use the manager available from page at host:port). Point your browser to <http://127.0.0.1:8080/semanlink> (if your running on localhost at port 8080). Enjoy!