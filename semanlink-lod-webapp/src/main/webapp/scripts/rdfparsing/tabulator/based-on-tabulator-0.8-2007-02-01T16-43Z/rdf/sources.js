/************************************************************
 * 
 * Project: AJAR/Tabulator
 * 
 * File: sources.js
 * 
 * Description: contains functions for requesting/fetching/retracting
 *  'sources' -- meaning any document we are trying to get data out of
 * 
 * SVN ID: $Id: sources.js,v 1.1 2009/03/02 08:32:18 fps Exp $
 *
 ************************************************************/

/**
 * Things to test: callbacks on request, refresh, retract
 *   loading from HTTP, HTTPS, FTP, FILE, others?
 */

function SourceFetcher(store, timeout, async) {
    this.store = store
    this.thisURI = "http://dig.csail.mit.edu/2005/ajar/ajaw/rdf/sources.js"
	           +"#SourceFetcher"
    this.timeout = timeout?timeout:30000
    this.async = async!=null?async:true
    this.appNode = this.store.bnode()
    this.requested = {}
    this.handlers = []
    this.mediatypes = {}
    var sf = this

    SourceFetcher.RDFXMLHandler = function (args) {
	if (args) {
	    this.dom = args[0]
	}
	this.recv = function (xhr) {
	    xhr.handle = function (cb) {
		var kb = sf.store
		if (!this.dom) {
		    var dparser = new DOMParser()
		    this.dom = dparser.parseFromString(xhr.responseText,
						       'application/xml')
		}
		var parser = new RDFParser(kb)
		parser.parse(this.dom, xhr.uri.uri, xhr.uri)
		
		cb()
	    }
	}
    }
    SourceFetcher.RDFXMLHandler.term = this.store.sym(this.thisURI
						      +".RDFXMLHandler")
    SourceFetcher.RDFXMLHandler.toString=function () { return "RDFXMLHandler" }
    SourceFetcher.RDFXMLHandler.register = function (sf) {
	sf.mediatypes['application/rdf+xml'] = {}
    }
    SourceFetcher.RDFXMLHandler.pattern = new RegExp("application/rdf\\+xml")

    SourceFetcher.XHTMLHandler = function (args) {
	if (args) {
	    this.dom = args[0]
	}
	this.recv = function (xhr) {
	    xhr.handle = function (cb) {
		if (!this.dom) {
		    var dparser = new DOMParser()
		    this.dom = dparser.parseFromString(xhr.responseText,
						       'application/xml')
		}
		var kb = sf.store
		
		// dc:title
		var title = this.dom.getElementsByTagName('title')
		if (title.length > 0) {
		    kb.add(xhr.uri,kb.sym('dc','title'),
			   kb.literal(title[0].textContent),xhr.uri)
		    log.info("Inferring title of "+xhr.uri)
		}

		// link rel
		var links = this.dom.getElementsByTagName('link')
		for (var x=links.length-1; x>=0; x--) {
		    if ((links[x].getAttribute('rel') == 'alternate'
			 || links[x].getAttribute('rel') == 'seeAlso'
			 || links[x].getAttribute('rel') == 'meta')
			&& links[x].getAttribute('href')) {
			var join = Util.uri.join
			var uri = kb.sym(join(links[x].getAttribute('href'),
					      xhr.uri.uri))
			kb.add(xhr.uri,kb.sym('rdfs','seeAlso'),uri,
			       xhr.uri)
			log.info("Loading "+uri+" from link rel in "+xhr.uri)
		    }
		}

		//GRDDL
		var head = this.dom.getElementsByTagName('head')[0]
		if (head) {
		    var profile = head.getAttribute('profile');
		    if (profile && Util.uri.protocol(profile)=='http') {
			log.info("GRDDL: Using generic "
				 + "2003/11/rdf-in-xhtml-processor.");
			sf.request(kb.sym('http://www.w3.org/2005/08/'
					  + 'online_xslt/xslt?'
					  + 'xslfile=http://www.w3.org'
					  + '/2003/11/'
					  + 'rdf-in-xhtml-processor'
					  + '&xmlfile='
					  + escape(xhr.uri.uri)),
				   xhr.uri)
		    } else {
			log.info("GRDDL: No GRDDL profile in "+xhr.uri)
		    }
		}

		cb()
	    }
	}
    }
    SourceFetcher.XHTMLHandler.term = this.store.sym(this.thisURI
						     +".XHTMLHandler")
    SourceFetcher.XHTMLHandler.toString=function () { return "XHTMLHandler" }
    SourceFetcher.XHTMLHandler.register=function (sf) {
	sf.mediatypes['application/xhtml+xml'] = {'q': 0.3}
    }
    SourceFetcher.XHTMLHandler.pattern = new RegExp("application/xhtml")
    
    SourceFetcher.XMLHandler = function () {
	this.recv = function (xhr) {
	    xhr.handle = function (cb) {
		var kb = sf.store
		var dom = (new DOMParser()).parseFromString(xhr.responseText,
							    'application/xml')

		// It could be RDF/XML
		// figure out the root element
		for (var c=0; c<dom.childNodes.length; c++) {
		    // is this node an element?
		    if (dom.childNodes[c].nodeType == 1) {
			// We've found the first element, it's the root
			if (dom.childNodes[c].namespaceURI
			    == kb.namespaces['rdf']) {
			    log.info(xhr.uri + " seems to have a root element"
				     + " in the RDF namespace. We'll assume "
				     + "it's RDF/XML.")
			    sf.switchHandler(SourceFetcher.RDFXMLHandler,
					     xhr, cb, [dom])
			    return
			}
			// it isn't RDF/XML or we can't tell
			break
		    }
                }

		// Or it could be XHTML?
		
		// Maybe it has an XHTML DOCTYPE?
                if (dom.doctype) {
		    log.info("We found a DOCTYPE in "+xhr.uri)
		    if (dom.doctype.name == 'html'
			&& dom.doctype.publicId.match(/^-\/\/W3C\/\/DTD XHTML/)
			&& dom.doctype.systemId.match(/http:\/\/www.w3.org\/TR\/xhtml/)) {
			log.info(xhr.uri + " has XHTML DOCTYPE. Switching to "
				 + "XHTML Handler.")
			sf.switchHandler(SourceFetcher.XHTMLHandler, xhr, cb)
			return
		    }
		}

		// Or what about an XHTML namespace?
		var html = dom.getElementsByTagName('html')[0]
		if (html) {
		    var xmlns = html.getAttribute('xmlns')
		    if (xmlns
			&& xmlns.match(/^http:\/\/www.w3.org\/1999\/xhtml/)) {
			log.info(xhr.uri + " has a default namespace for "
				 + "XHTML. Switching to XHTMLHandler.")
			sf.switchHandler(SourceFetcher.XHTMLHandler, xhr, cb)
			return
		    }
		}

		// We give up. What dialect is this?
		sf.failFetch(xhr, "unsupportedDialect")
	    }
	}
    }
    SourceFetcher.XMLHandler.term = this.store.sym(this.thisURI
						   +".XMLHandler")
    SourceFetcher.XMLHandler.toString=function () { return "XMLHandler" }
    SourceFetcher.XMLHandler.register = function (sf) {
	sf.mediatypes['text/xml'] = {'q': 0.2}
	sf.mediatypes['application/xml'] = {'q': 0.2}
    }
    SourceFetcher.XMLHandler.pattern = new RegExp("(text|application)/(.*)xml")

    SourceFetcher.HTMLHandler = function () {
	this.recv = function (xhr) {
	    xhr.handle = function (cb) {
		var rt = xhr.responseText
		// We only handle XHTML so we have to figure out if this is XML
		log.info("Sniffing HTML "+xhr.uri+" for XHTML.");

		// XML declaration
		if (rt.match(/\s*<\?xml\s+version\s*=[^<>]+\?>/)) {
		    log.info(xhr.uri + " has an XML declaration. We'll assume "
			     + "it's XHTML as the content-type was text/html.")
		    sf.switchHandler(SourceFetcher.XHTMLHandler, xhr, cb)
		    return
		}

		// DOCTYPE
		// There is probably a smarter way to do this
		if (rt.match(/.*<!DOCTYPE\s+html[^<]+-\/\/W3C\/\/DTD XHTML[^<]+http:\/\/www.w3.org\/TR\/xhtml[^<]+>/)) {
		    log.info(xhr.uri + " has XHTML DOCTYPE. Switching to XHTML"
			     + "Handler.")
		    sf.switchHandler(SourceFetcher.XHTMLHandler, xhr, cb)
		    return
		}

		// xmlns
		if (rt.match(/[^(<html)]*<html\s+[^<]*xmlns=['"]http:\/\/www.w3.org\/1999\/xhtml["'][^<]*>/)) {
		    log.info(xhr.uri + " has a default namespace for XHTML."
			     + " Switching to XHTMLHandler.")
		    sf.switchHandler(SourceFetcher.XHTMLHandler, xhr, cb)
		    return
		}

		sf.failFetch(xhr, "can't parse non-XML HTML")
	    }
	}
    }
    SourceFetcher.HTMLHandler.term = this.store.sym(this.thisURI
						    +".HTMLHandler")
    SourceFetcher.HTMLHandler.toString=function () { return "HTMLHandler" }
    SourceFetcher.HTMLHandler.register = function (sf) {
	sf.mediatypes['text/html'] = {'q': 0.3}
    }
    SourceFetcher.HTMLHandler.pattern = new RegExp("text/html")
    
    SourceFetcher.TextHandler = function () {
	this.recv = function (xhr) {
	    xhr.handle = function (cb) {
		// We only speak dialects of XML right now. Is this XML?
		var rt = xhr.responseText

		// Look for an XML declaration
		if (rt.match(/\s*<\?xml\s+version\s*=[^<>]+\?>/)) {
		    log.warn(xhr.uri + " has an XML declaration. We'll assume "
			     + "it's XML but its content-type wasn't XML.")
		    sf.switchHandler(SourceFetcher.XMLHandler, xhr, cb)
		    return
		}
		
		// Look for an XML declaration
		if (rt.slice(0,500).match(/xmlns:/)) {
		    log.warn(xhr.uri + " may have an XML namespace. We'll assume "
			     + "it's XML but its content-type wasn't XML.")
		    sf.switchHandler(SourceFetcher.XMLHandler, xhr, cb)
		    return
		}
		
		// We give up
		sf.failFetch(xhr, "unparseable - text/plain not visibly XML")
		log.warn(xhr.uri + " unparseable - text/plain not visibly XML, starts:\n"
			+ rt.slice(0,500))

	    }
	}
    }
    SourceFetcher.TextHandler.term = this.store.sym(this.thisURI
						    +".TextHandler")
    SourceFetcher.TextHandler.toString = function () { return "TextHandler" }
    SourceFetcher.TextHandler.register = function (sf) {
	sf.mediatypes['text/plain'] = {'q': 0.1}
    }
    SourceFetcher.TextHandler.pattern = new RegExp("text/plain")

    Util.callbackify(this,['request', 'recv', 'load', 'fail', 'refresh',
			   'retract', 'done'])

    this.store.register('rdfs', "http://www.w3.org/2000/01/rdf-schema#")
    this.store.register('owl', "http://www.w3.org/2002/07/owl#")
    this.store.register('tab',"http://dig.csail.mit.edu/2005/ajar/ajaw/ont#")
    this.store.register('http',"http://dig.csail.mit.edu/2005/ajar/ajaw/http#")
    this.store.register('httph',
			"http://dig.csail.mit.edu/2005/ajar/ajaw/httph#")
    this.store.register('ical',"http://www.w3.org/2002/12/cal/icaltzd#")

    this.addProtocol = function (proto) {
	sf.store.add(sf.appNode,
		     sf.store.sym("tab","protocol"),
		     sf.store.literal(proto),
		     this.appNode)
    }

    this.addHandler = function (handler) {
	sf.handlers.push(handler)
	handler.register(sf)
    }

    this.switchHandler = function (handler, xhr, cb, args) {
	var kb = this.store;
	(new handler(args)).recv(xhr);
	kb.the(xhr.req,kb.sym('tab','handler')).append(handler.term)
	xhr.handle(cb)
    }

    this.addStatus = function (xhr, status) {
	var kb = this.store
	kb.the(xhr.req,kb.sym('tab',"status")).append(kb.literal(status))
    }

    this.failFetch = function (xhr, status) {
	this.addStatus(xhr,status)
	this.requested[Util.uri.docpart(xhr.uri.uri)] = false
	this.fireCallbacks('fail',[xhr.requestedURI])
	xhr.abort()
    }

    this.seeAlsoFetch = function (xhr, args, status, uri) {
	xhr.abort()
	xhr.aborted = true
	var kb = this.store
	this.addStatus(xhr,"Redirected: "+status)
	this.addStatus(xhr,'done')
	kb.add(args[0],kb.sym('rdfs',"seeAlso"),kb.sym(uri),xhr.uri)
	this.fireCallbacks('done',args)
	this.request(kb.sym(uri),xhr.uri,args[2])
    }

    this.redirectFetch = function (xhr, args, status, uri) {
	xhr.abort()
	xhr.aborted = true
	var kb = this.store
	this.addStatus(xhr,"Redirected: "+status)
	this.addStatus(xhr,'done')
	kb.add(args[0],kb.sym('owl',"sameAs"),kb.sym(uri),xhr.uri)
	this.fireCallbacks('done',args)
	this.request(kb.sym(uri),xhr.uri,args[2])
    }

    this.doneFetch = function (xhr, args) {
	this.addStatus(xhr,'done')
	log.info("Done with parse, firing 'done' callbacks for "+xhr.uri)
	this.fireCallbacks('done',args)
    }

    this.store.add(this.store.sym('http://dig.csail.mit.edu/2005/ajar/ajaw/data#Tabulator'),
		   this.store.sym('tab',"session"),
		   this.appNode,
		   this.appNode)
    this.store.add(this.appNode,
		   this.store.sym('rdfs','label'),
		   this.store.literal('This Session'),
		   this.appNode);

    ['http','https','file'].map(this.addProtocol); // ftp?

    [SourceFetcher.RDFXMLHandler,
     SourceFetcher.XHTMLHandler,
     SourceFetcher.XMLHandler,
     SourceFetcher.HTMLHandler,
     SourceFetcher.TextHandler].map(this.addHandler)

    this.addCallback('done',function (uri, r) {
			 var kb = sf.store
			 var udoc=uri.uri?kb.sym(Util.uri.docpart(uri.uri)):uri

			 var seeAlso = sf.store.sym('rdfs','seeAlso')
			 var refs = sf.store.statementsMatching(uri,seeAlso)
			 refs.map(function (x) {
				      if (!sf.requested[Util.uri.docpart(x.object.uri)]) {
					  sf.request(x.object,uri)
				      }
				  })
				  
			 var sameAs = sf.store.sym('owl','sameAs') // @@ neaten up
			 var refs = sf.store.statementsMatching(uri,sameAs)
			 refs.map(function (x) {
				      if (!sf.requested[Util.uri.docpart(x.object.uri)]) {
					  sf.request(x.object,uri)
				      }
				  })
				  
			 var refs = sf.store.statementsMatching(undefined, sameAs, uri)
			 refs.map(function (x) {
				      if (!sf.requested[Util.uri.docpart(x.subject.uri)]) {
					  sf.request(x.subject,uri)
				      }
				  })
				  
			 var type = sf.store.sym('rdf','type')
			 var mentions = sf.store.sym('tab','mentionsClass')
			 var refs = sf.store.statementsMatching(undefined,
								type,
								undefined,
								udoc)
			 refs.map(function (x) {
				      sf.store.add(udoc,mentions,x.object,udoc)
				  })
			 return true
		     })

    /** Requests a URI and loads it. */
    this.request = function (term, rterm, force) { //sources_request_new
	var force = !!force
	var kb = this.store
	var args = arguments

	this.fireCallbacks('request',args)

	if (term.uri) { // we have a URI instead of a blank node
	    var uri = kb.sym(Util.uri.docpart(term.uri))
	} else {
	    log.info(uri + " isn't a resource to fetch. Skipping.")
	    this.fireCallbacks('done',args)
	    return
	}

	if (!force && typeof this.requested[uri.uri]!="undefined") {
	    log.debug("We already have "+uri+". Skipping.")
	    this.fireCallbacks('done',args)
	    return
	}

	this.requested[uri.uri] = true

	if (rterm) {
	    if (rterm.uri) {
		kb.add(uri, kb.sym('tab',"requestedBy"),
		       kb.sym(Util.uri.docpart(rterm.uri)), this.appNode)
	    } else {
		kb.add(uri, kb.sym('tab',"requestedBy"),
		       rterm, this.appNode)
	    }
	}

	var status = kb.collection()
	var xhr = Util.XMLHTTPFactory()
	var req = xhr.req = kb.bnode()
	xhr.uri = uri
	xhr.requestedURI = args[0]
	var handlers = kb.collection()
	var sf = this

	kb.add(this.appNode, kb.sym('tab',"source"), uri, this.appNode)
	kb.add(uri, kb.sym('tab',"request"), req, this.appNode)
	kb.add(req, kb.sym('rdfs',"label"), kb.literal('Request for '+uri),
	       this.appNode)

	// This request will have handlers probably
	kb.add(req, kb.sym('tab','handler'), handlers, sf.appNode)

	kb.add(req, kb.sym('tab','status'), status, sf.appNode)

	if (typeof kb.anyStatementMatching(this.appNode,
					   kb.sym('tab',"protocol"),
					   Util.uri.protocol(uri.uri))
	    == "undefined") {
	    // update the status before we break out
	    this.failFetch(xhr,"Unsupported protocol")
	    return
	}

	// Set up callbacks
	xhr.onreadystatechange = function () {
	    switch (xhr.readyState) {
	    case 3:
		if (!xhr.recv) {
		    xhr.recv = true
		    var handler = null
		    
		    sf.fireCallbacks('recv',args)
		    
		    kb.add(req,kb.sym('http','status'),kb.literal(xhr.status),
			   sf.appNode)
		    kb.add(req,kb.sym('http','statusText'),
			   kb.literal(xhr.statusText), sf.appNode)
		    
		    if (xhr.status >= 400) {
			sf.failFetch(xhr,"HTTP error "+xhr.status+ ' '+
			    xhr.statusText )
			break
		    }
		    
		    xhr.headers = {}
		    if (Util.uri.protocol(xhr.uri.uri) == 'http'
		        || Util.uri.protocol(xhr.uri.uri) == 'https') {
			xhr.headers = Util.getHTTPHeaders(xhr)
			for (var h in xhr.headers) {
			    kb.add(req, kb.sym('httph',h), xhr.headers[h],
				   sf.appNode)
			}
		    }

		    if (Util.uri.protocol(xhr.uri.uri) == 'file') {
			log.info("Assuming local file is some flavor of XML.")
			xhr.headers['content-type'] = 'text/xml'
		    }
		    
		    var loc = xhr.headers['content-location']

		    if (loc) {
			var udoc = Util.uri.join(xhr.uri.uri,loc)
			if (!force && udoc != xhr.uri.uri 
			    && sf.requested[udoc]) {
			    // should we smush too?
			    log.info("HTTP headers indicate we have already"
				     + " retrieved " + xhr.uri + " as "
				     + udoc + ". Aborting.")
			    sf.doneFetch(xhr,args)
			    xhr.abort()
			    break
			}
			sf.requested[udoc] = true
		    }

		    for (var x = 0; x<sf.handlers.length; x++) {
			if (xhr.headers['content-type'].match(sf.handlers[x].pattern)){
			    handler = new sf.handlers[x]()
			    handlers.append(sf.handlers[x].term)
			    break
			}
		    }
		    
		    if (handler) {
			handler.recv(xhr)
		    } else {
			sf.failFetch(xhr,"Unhandled content type: " +
			    xhr.headers['content-type']);
			break
		    }
		}
		break
	    case 4:
		// Now handle
		if (xhr.handle) {
		    xhr.handle(function () {
				   sf.doneFetch(xhr,args)
			       })
		    sf.fireCallbacks('load',args)
		}
		break
	    }
	}

	// Get privileges for cross-domain XHR
	try {
	    Util.enablePrivilege("UniversalXPConnect UniversalBrowserRead")
        } catch(e) {
	    alert("Failed to get privileges: " + e)
	}

	// Setup the request
	xhr.open('GET', uri.uri, this.async)

	// Set redirect callback and request headers
	if (Util.uri.protocol(xhr.uri.uri) == 'http'
	    || Util.uri.protocol(xhr.uri.uri) == 'https') {
	    try {
		xhr.channel.notificationCallbacks = {
		    getInterface: function (iid) {
			Util.enablePrivilege("UniversalXPConnect")
			if (iid.equals(Components.interfaces.nsIChannelEventSink)) {
			    return {
				onChannelRedirect: function (oldC,newC,flags) {
				    Util.enablePrivilege("UniversalXPConnect")
				    if (xhr.aborted) return
				    if (xhr.status==302 || xhr.status==303) {
					sf.seeAlsoFetch(xhr,
							args,
							xhr.status + " to <"
							+ newC.URI.spec + ">",
							newC.URI.spec)
				    } else {
					sf.redirectFetch(xhr,
							 args,
							 xhr.status + " to <"
							 + newC.URI.spec + ">",
							 newC.URI.spec)
				    }
				}
			    }
			}
			return Components.results.NS_NOINTERFACE
		    }
		}
	    } catch (err) {
		alert("Couldn't set callback for redirects: "+err)
	    }

	    try {
		var acceptstring = ""
		for (var type in this.mediatypes) {
		    var attrstring = ""
		    if (acceptstring != "") { acceptstring += ", " }
		    acceptstring += type
		    for (var attr in this.mediatypes[type]) {
			acceptstring += ';' + attr + '='
			    + this.mediatypes[type][attr]
		    }
		}
		xhr.setRequestHeader('Accept',acceptstring)
//		twarn('Accept: '+ acceptstring)
		
		// See http://dig.csail.mit.edu/issues/tabulator/issue65
		//if (requester) { xhr.setRequestHeader('Referer',requester) }
	    } catch (err) {
		alert("Can't set Accept header: "+err)
	    }
	}

	// Fire
	try {
	    xhr.send(null)
	} catch (er) {
	    this.failFetch(xhr,"sendFailed")
	    return
	}

	// Drop privs
	try {
	    Util.disablePrivilege("UniversalXPConnect UniversalBrowserRead")
	} catch (e) {
	    alert("Can't drop privilege: " + e)
	}

	setTimeout(function() { 
		       if (xhr.readyState != 4 && sf.isPending(xhr.uri)) {
			   sf.failFetch(xhr,"requestTimeout")
		       }
		   }, this.timeout)
    }

    this.refresh = function (uri) { // sources_refresh
	this.store.removeMany(undefined,undefined,undefined,uri)
	this.fireCallbacks('refresh',arguments)
	this.request(uri, undefined, true)
    }

    this.retract = function (uri) { // sources_retract
	this.store.removeMany(undefined,undefined,undefined,uri)
	if (uri.uri) {
	    delete this.requested[Util.uri.docpart(uri.uri)]
	}
	this.fireCallbacks('retract',arguments)
    }

    this.getState = function (uri) { // docState
	var doc = Util.uri.docpart(uri.uri)
	if (typeof this.requested[doc] != "undefined") {
	    if (this.requested[doc]) {
		if (this.isPending(uri)) {
		    return "requested"
		} else {
		    return "fetched"
		}
	    } else {
		return "failed"
	    }
	} else {
	    return "unrequested"
	}
    }

    this.isPending = function (uri) { // sources_pending
	var req = this.store.anyStatementMatching(
	    this.store.sym(Util.uri.docpart(uri.uri)),
	    this.store.sym('tab','request'))
	if (!req) { return false }
	var status = this.store.anyStatementMatching(req.object,
						     this.store.sym('tab',
								    'status'))
	if (!status) { return true }
	return (this.requested[Util.uri.docpart(uri.uri)]
		&& !status.object.elements.filter(function (x) {
						      return (x.toString()
							      == 'done')
						  }).length)
    }
}

/*
remote.js: refreshButtons, sources_check_callbacks, sources_xml

    function loadSPARQLEndpoints(subject) {
 	if (!subject.uri) return;
 	var sparqlEndpoints = kb.statementsMatching(undefined, kb.sym(
							'http://dig.csail.mit.edu/2005/ajar/ajaw/ont#sparqlEndpoint'), undefined, kb.sym(uri_docpart(subject.uri)))
	for (var x=0;x<sparqlEndpoints.length;x++)
	{
	    ep = sparqlEndpoints[x];
	    if (ep.subject.uri && ep.object.uri) {
		tdebug("Retrieving data for "+subject+" from SPARQL endpoint at "+ep.uri);
		kb.spEndpointIndex[ep.subject.uri]=ep.object.uri;
	    }
	}	
    }
*/

function isExternal (uri)
{
	for (x in kb.spEndpointIndex)
		if (uri.match(new RegExp('^'+x))) return kb.spEndpointIndex[x]; //redirect to external
}

function sources_check_callbacks()
{
    for (var t in sources.callbacks) { //for each trigger
        tdebug("trigger=" + t + ", depends on=" + sources.depends[t]);
        if (sources.depends[t] && filter(sources_pending, sources.depends[t]).length != 0)
            continue; //still dependencies
        tsuccess("all dependencies loaded for " + t + ", completed files=" + sources.depends[t]);
        if (!sources.callbacks[t])
            tinfo("no callback for trigger " + t);
        else {
            for (var c in sources.callbacks[t]) {
                tdebug("executing callback #" + c + " for: " + t); // + ", " + sources.callbacks[t][c]);
                sources.callbacks[t][c](); //call back
            } //for
            delete sources.callbacks[t]; //unset this trigger
        } //has callbacks
    } //for each trigger
}

/** hack around firefox permission shtuff **/   
function sources_xml(request) {
    return (new DOMParser()).parseFromString(request.responseText, 'text/xml');
} //sources_xml

/** completely(!) retrach a source from the kb **/
function sources_retract(uri)
{ 
    var src = kb.sym(uri_docpart(uri));
    sources_remove_html(sources.status[uri].number);
    kb.removeMany(undefined, undefined, undefined, src); //no limit
    sources.status[uri] = undefined;
    refreshButtons(uri, icon_unrequested); //as if it had never been
} //retract

/** fetch an RDFSymbol (OVERWRITE old requestFetch) use request_new **/
function requestFetch(subject) {
    sources_request_new(subject);
} //requestFetch

/*
function sources_add_html(uri) {
    var i = sources.status[uri].number; //index
    var x = document.getElementById('sources');
    if (!x) terror("no sources table!");
    if (x) {
        var addendum = document.createElement("tr");
        addendum.setAttribute('class', 'requested');
        addendum.setAttribute('id', 'source'+i);
        
        var td = document.createElement('td');
        addendum.appendChild(td);
        var tn = document.createTextNode(uri);
        td.appendChild(tn);
        td.setAttribute('id','Source:'+uri);
	alert("@@ seting id on source tr: "+ 'Source:'+uri) 
        td = document.createElement('td');
        addendum.appendChild(td);
        tn = document.createTextNode('requested');
        td.appendChild(tn);
        var actions = document.createElement('td');
        
        var retr = AJARImage(icon_retract, 'retract');
        var refr = AJARImage(icon_refresh, 'refresh');
        addEvent(retr, 'mousedown', function() { sources_retract(uri); });
        addEvent(refr, 'mousedown', function() { sources_refresh(uri); });
        //retr.setAttribute('onmousedown', 'sources_retract("'+uri+'")');
        //refr.setAttribute('onmousedown', 'sources_refresh("'+uri+'")');
        actions.appendChild(retr);
        actions.appendChild(refr);
        addendum.appendChild(actions);
        
        x.appendChild(addendum);
        }
} //sources_add_html

function sources_remove_html(index)
{
    var row = document.getElementById('source'+index);
    if (!row) {
        twarn("no such source: " + index);
        return;
    }
    row.parentNode.removeChild(row);
} //sources_remove_html

*/

/** update the sources table **/
/*
function sources_update_html(uri, status, external) {
	if (external) var uri=externalSource(uri,external)
    var mystate = sources.status[uri].state, myindex = sources.status[uri].number;
    var x = document.getElementById('source'+myindex);
    if (!x)
        alert("What? no source"+myindex);
    else
        x.setAttribute('class', mystate);
    var td = x.childNodes[1];
    var tn = td.childNodes[0];
    tinfo("td="+td+" tn="+tn+" response="+status);
    td.replaceChild(document.createTextNode(status), tn);
} //sources_update_html

*/