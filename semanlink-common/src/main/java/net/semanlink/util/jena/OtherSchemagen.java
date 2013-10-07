/* Created on 30 oct. 08 */
package net.semanlink.util.jena;
/*****************************************************************************
 * Source code information
 * -----------------------
 * Changes made by fps to Jena's schemagen in order to 
 * Original author    fps@semanlink.net
 * -----------------------
 * BASED ON : Schemagen
 * Original author    Ian Dickinson, HP Labs Bristol
 * Author email       Ian.Dickinson@hp.com
 * Package            Jena 2
 * Web                http://sourceforge.net/projects/jena/
 * Created            14-Apr-2003
 * Filename           $RCSfile: OtherSchemagen.java,v $
 * Revision           $Revision: 1.5 $
 * Release status     $State: Exp $
 *
 * Last modified on   $Date: 2010/06/03 14:15:47 $
 *               by   $Author: fps $
 *
 * (c) Copyright 2002, 2003, 2004, 2005, 2006, 2007, 2008 Hewlett-Packard Development Company, LP
 * (see footer for full conditions)
 *****************************************************************************/

import java.io.*;

import com.hp.hpl.jena.rdf.model.*;

import jena.schemagen;

/**
 * Idem jena.schemagen, except that <ul>
 * <li> it allows to define the (short) java name of java objects in the model,
 * using a  javaClass property </li>
 * <li> it uses the declared NS instead of long URIs all over the file</li>
 * </ul>
 */
public class OtherSchemagen extends schemagen {
// always use getter
private String namespaceURI;
/* Main entry point. See schemagen's Javadoc for details of the many command line arguments */
public static void main( String[] args ) {
	System.out.println("OtherSchemagen.main !!!!");
	new OtherSchemagen().go( args );
}

protected Property getJavaClassNameProperty(Model sourceModel) {
	return sourceModel.createProperty(getNamespaceURI() + "javaName");
}

protected String getNamespaceURI() {
	if (this.namespaceURI == null) this.namespaceURI = determineNamespaceURI();
	return this.namespaceURI ;
}

/** Answer the Java value name for the URI 
 *  This is exactly the same as in schemagen EXCEPT 
 *  - for the first line of method
 *  - adding "_PROP" to the name (if uppercased name, and not defined in schema with special prop)
 *  (this is because we want to add "_PROP" to the name
 *  and we cannot safely use the templates for that, because
 *  if we have a Toto class and a toto prop, once converted to uppercase
 *  schemagen thinks we get a clash of identifiers and we ends up with
 *  TOTO_TYPE_CLASS and/or TOTO_PROP_PROP
 *  */
protected String getValueName( Resource r, String disambiguator ) {
	// This comes from original schemagen
	// the id name is basically the local name of the resource, possibly in upper case
	// B4 jena 2.6.3 
	// String name = isTrue( OPT_UC_NAMES ) ? getUCValueName( r ) : r.getLocalName();
	String name = getValueNameDefinedInModel(r);
	if (name == null) {
		// name = isTrue( OPT_UC_NAMES ) ? getUCValueName( r ) : r.getLocalName(); // B4 jena 2.6.3 (or even b4 ?)
		// if (isTrue( OPT_UC_NAMES )) {
		// name = m_options.hasUcNamesOption() ? getUCValueName( r ) : r.getLocalName(); // jena 2.6.3
		if (m_options.hasUcNamesOption()) {
			name = getUCValueName( r );
			if ("_PROP".equals(disambiguator)) {
				name = name + disambiguator;
			}
		}
	}	
	
	// must be legal java
	name = asLegalJavaID( name, false );

	// must not clash with an existing name
	int attempt = 0;
	String baseName = name;
	while (m_usedNames.contains( name )) {
		name = (attempt == 0) ? (name + disambiguator) : (baseName + disambiguator + attempt);
		attempt++;
	}

	// record this name so that we don't use it again (which will stop the vocabulary from compiling)
	m_usedNames.add( name );

	// record the mapping from resource to name
	m_resourcesToNames.put( r, name );

	return name;
}

/** If the model contains a Java value name for r, return it, else return null */
protected String getValueNameDefinedInModel( Resource r ) {
	// http://.../docservice/schema/rdc-schema.owl#javaName
	// System.out.println(r.getURI() + " : " + getJavaClassNameProperty(this.m_source).getURI());
	NodeIterator it = this.m_source.listObjectsOfProperty(r, getJavaClassNameProperty(this.m_source));
	if (it.hasNext()) {
		String name = it.next().toString();
		it.close();
		return name;
	}
	return null;
}

//
// REPLACING LONG URIs, USING THE NAMESPACE
//

/** Changes what is done in schemagen to use the namespace in URIs */
protected void writeValue( Resource r, String template, String valueClass, String creator, String disambiguator ) {
	if (!filter( r )) {
		if (!noComments()  &&  hasComment( r )) {
			writeln( 1, formatComment( getComment( r ) ) );
		}

		// push the local bindings for the substitution onto the stack
		
		// this was in schemagen:
		// addReplacementPattern( "valuri", r.getURI() );
		
		// problem is that the template
		// public static final String DEFAULT_TEMPLATE = "public static final %valclass% %valname% = m_model.%valcreator%( \"%valuri%\" );";
		// contains the quotes around the uri, (they are not part of valuri)
		// giving things such as
		// m_model.createOntProperty( "http://.../schema#myprop" );
		// We cannot use this template as it is (we would get m_model.createOntProperty( "NS+"myprop"" );
		// We must first change the template to include the quotes surrounding ?valuri% into valuri
		// replaced by:
		
		String uri = r.getURI();
		boolean nsRemovedFromUri = false;
		String ns = getNamespaceURI();
		if (uri.startsWith(ns)) {
			String valuriBetweenQuotesInTemplate = "\"%valuri%\"";
			int k = template.indexOf(valuriBetweenQuotesInTemplate);
			if (k > -1) {
				nsRemovedFromUri = true;
				String niceUri = " NS + \"" + uri.substring(ns.length()) + "\"";
				String newTemplate = template.substring(0,k-1) + "%valuri%" + template.substring(k+valuriBetweenQuotesInTemplate.length());
				template = newTemplate;
				addReplacementPattern( "valuri", niceUri);
			}
		}
		if (!nsRemovedFromUri) addReplacementPattern( "valuri", uri );

		addReplacementPattern( "valname", getValueName( r, disambiguator ));
		addReplacementPattern( "valclass", valueClass );
		addReplacementPattern( "valcreator", creator );

		// write out the value
		writeln( 1, substitute( template ) );
		writeln( 1 );

		// pop the local replacements off the stack
		pop( 4 );
	}
}

//
// Supporting UTF8 chars
//

protected PrintWriter m_writer;

/** Identify the file we are to write the output to */
protected void selectOutput() {
  String outFile = m_options.getOutputOption();
    if (outFile == null) {
        m_writer = new PrintWriter(System.out);
    }
    else {
        try {
            File out = new File( outFile );
 
            if (out.isDirectory()) {
                // create a file in this directory named classname.java
                String fileName = outFile + System.getProperty( "file.separator" ) + getClassName() + ".java";
                out = new File( fileName );
            }
           System.out.println("OtherSchemagen will write to file: " + out);
           m_writer = new PrintWriter(new OutputStreamWriter(new FileOutputStream(out),"UTF-8"));
        }
        catch (Exception e) {
            abort( "I/O error while trying to open file for writing: " + outFile, e );
        }
    }

    // check for DOS line endings
    if (m_options.hasDosOption()) {
        m_nl = "\r\n";
    }
}

/** Close the output file */
protected void closeOutput() {
	m_writer.flush();
	m_writer.close();
}

/** Write out the given string with n spaces of indent, with newline */
protected void writeln( int indent, String s ) {
    write( indent, s );
    m_writer.print( m_nl );
}

/** Write out the given string with n spaces of indent */
protected void write( int indentLevel, String s ) {
    for (int i = 0;  i < (m_indentStep * indentLevel);  i++) {
    	m_writer.print( " " );
    }

    m_writer.print( s );
}

//
// TO PASS THE NS AS PARAMETER TO CONSTRUCTOR
//

/** The opening class declaration */
protected void writeClassDeclaration() {
    write( 0, "public class " );
    write( 0, getClassName() );
    write( 0, " " );

    if (m_options.hasClassdecOption()) {
      write( 0, m_options.getClassdecOption() );
    }

    writeln( 0, "{" );
    
    /*
    // fps
    
    writeln( 0, "private String ns;");
    write( 0, "public ");
    write( 0, getClassName() );
    writeln( 0, "(String ns) {");
    writeln( 1, "this.ns = ns;");
    writeln( 0, "}" );
    */

}

//
//
//

}
